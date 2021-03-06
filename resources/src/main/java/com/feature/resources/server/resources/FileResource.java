package com.feature.resources.server.resources;

import com.feature.resources.server.domain.Graphic;
import com.feature.resources.server.domain.WorkSpace;
import com.feature.resources.server.dto.CheckResult;
import com.feature.resources.server.dto.CheckStatusDesc;
import com.feature.resources.server.dto.FileMeta;
import com.feature.resources.server.dto.FileUrl;
import com.feature.resources.server.service.GraphicService;
import com.feature.resources.server.service.WorkSpaceService;
import com.feature.resources.server.util.DomainObjectFactory;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

@Path("/file")
public class FileResource extends Resource{
    public static final Logger LOGGER = LoggerFactory.getLogger(FileResource.class);

    @Inject
    private GraphicService graphicService;
    @Inject
    private WorkSpaceService workSpaceService;

    @Inject
    private DomainObjectFactory objectFactory;

    @Inject
    @Named("audit.auto")
    private boolean isAutoAudit;

    private String tagId;
    private String workspaceId;

    /* step 1. get a unique url */
    @GET
    @Path("/url")
    public Response getCallBackUrl() {
        String url = "resources/rs/file";
        return Response.ok(new FileUrl(url)).build();
    }

    /* step 2. post a file */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @SuppressWarnings(value = "unchecked")
    public void post(@Context HttpServletRequest req, @Context HttpServletResponse res) throws IOException, FileUploadException, ServletException {

        String key = "xxx";
        ServletFileUpload uploadHandler = new ServletFileUpload(new DiskFileItemFactory());
        List<FileItem> fileItems = uploadHandler.parseRequest(req);
        getCurrentUserFromSession();
        for (FileItem fileItem : fileItems) {
            if (!fileItem.isFormField()) {
                String fileName = fileItem.getName();
                long size = fileItem.getSize();
                String contentType = fileItem.getContentType();
                LOGGER.info("fileName:" + fileName + " size:" + size + " contentType:" + contentType);
                if(StringUtils.isEmpty(workspaceId)){
                    WorkSpace workSpace =workSpaceService.getDefaultWorkSpace(shiroUser.getUserId());
                    workspaceId = workSpace.getIdString();
                }
                Graphic graphic = graphicService.generateGraphic(fileName, size, contentType,tagId,workspaceId);
                graphic.setUserId(shiroUser.getUserId());
                byte[] bytes = fileItem.get();
                graphic.setCheckStatus(CheckStatusDesc.UNCHECKED.getValue());
                if(isAutoAudit){
                    graphic.setCheckStatus(CheckStatusDesc.CHECKED.getValue());
                    graphic.setCheckResult(CheckResult.PASS.getValue());
                }
                key = graphicService.dealUploadDataToCreateNewGraphic(bytes, graphic );
            }else{
                getWorkSpaceAndTagIdInfoFromUPloadFormData(fileItem);
            }
        }
        res.sendRedirect("file/" + key + "/meta");
    }


    private void getWorkSpaceAndTagIdInfoFromUPloadFormData(FileItem fileItem) {
        String fieldName =  fileItem.getFieldName();
        if("tag".equals(fieldName)){
            tagId = fileItem.getString();
            LOGGER.info(tagId);
        }
        if("workSpace".equals(fieldName)){
            workspaceId = fileItem.getString();
            LOGGER.info(workspaceId);
        }
    }


    /* step 3. redirected to the meta info */
    @GET
    @Path("/{key}/meta")
    @Produces({MediaType.APPLICATION_JSON})
    public Response redirect(@PathParam("key") String key) {
        LOGGER.info("Key:" + key);
        Graphic graphic = graphicService.get(key);
        if (graphic == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        FileMeta fileMeta = objectFactory.createFileMeta(graphic);
        fileMeta.setCheckStatusDesc(CheckStatusDesc.valueOf(graphic.getCheckStatus()));
        if(graphic.getCheckResult() != null){
            fileMeta.setCheckResult(CheckResult.valueOf(graphic.getCheckResult()));
        }
        List<FileMeta> metaList = Lists.newArrayList(fileMeta);
        GenericEntity<List<FileMeta>> entity = new GenericEntity<List<FileMeta>>(metaList) {  };
        return Response.ok(entity).build();
    }

    /* step 4. download the file */
    @GET
    @Path("/{key}")
    public Response server(@PathParam("key") String key, @Context HttpServletResponse response) {
        response.setHeader("Content-Disposition", "attachment;filename=" + "name.png");
        return Response.ok("").build();
    }

    @DELETE
    @Path("/{key}")
    public Response delete(@PathParam("key") String key) {
        LOGGER.info("Delete file:" + key);
        Response.Status status;
        if (StringUtils.isEmpty(key)) {
            status = Response.Status.BAD_REQUEST;
        } else {
            graphicService.delete(key);
            status = Response.Status.OK;
        }
        return Response.status(status).build();
    }

    @HEAD
    public Response head() {
        return Response.ok("support").build();
    }

}
