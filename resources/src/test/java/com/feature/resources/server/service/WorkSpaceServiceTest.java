package com.feature.resources.server.service;

import com.feature.resources.server.dao.WorkSpaceDao;
import com.feature.resources.server.domain.WorkSpace;
import com.feature.resources.server.dto.WorkSpaceDTO;
import com.feature.resources.server.service.impl.WorkSpaceServiceImpl;
import com.google.code.morphia.Datastore;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.bson.types.ObjectId;
import org.fest.assertions.Assertions;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.jukito.TestSingleton;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: ZouYanjian
 * Date: 12-6-19
 * Time: 上午9:52
 * FileName:WorkSpaceServiceTest
 */
@RunWith(JukitoRunner.class)
public class WorkSpaceServiceTest {
    @Inject
    WorkSpaceService workSpaceService;

    public static class Module extends JukitoModule {

        @Override
        protected void configureTest() {
            forceMock(Datastore.class);
            bindMock(WorkSpaceDao.class).in(TestSingleton.class);
            bind(WorkSpaceService.class).to(WorkSpaceServiceImpl.class).in(TestSingleton.class);
        }
    }

    @Before
    public void setUp(WorkSpaceDao workSpaceDao) throws Exception {
        Mockito.when(workSpaceDao.isAlreadyExists("name", "test")).thenReturn(true);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void should_add_new_workspace_successful(WorkSpaceDao workSpaceDao) {
        WorkSpaceDTO workspaceDTO = new WorkSpaceDTO();
        workspaceDTO.setName("hello");
        workSpaceService.addNewWorkspace(workspaceDTO);
        verify(workSpaceDao).save(Mockito.any(WorkSpace.class));
    }

    @Test
    public void should_return_true(WorkSpaceDao mockDao) {
        boolean result = workSpaceService.exists("test");
        verify(mockDao).isAlreadyExists("name", "test");
        assertThat(result).isEqualTo(true);
    }

    @Test
    public void should_return_false(WorkSpaceDao mockDao) {
        when(mockDao.isAlreadyExists("name", "xxxx")).thenReturn(false);
        boolean result = workSpaceService.exists("xxxx");
        verify(mockDao).isAlreadyExists("name", "xxxx");
        assertThat(result).isEqualTo(false);
    }

    @Test
    public void should_return_workspace_list(WorkSpaceDao mockDao) {
        List<WorkSpace> workSpaces = Lists.newArrayList();
        WorkSpace workSpace = new WorkSpace();
        workSpace.setName("text");
        ObjectId id = new ObjectId();
        workSpace.setId(id);
        workSpaces.add(workSpace);
        when(mockDao.getEntityList()).thenReturn(workSpaces);
        List<WorkSpaceDTO> workSpaceList = workSpaceService.getCurrentWorkSpaceList();
        Assert.assertNotNull(workSpaceList);
        Assertions.assertThat(workSpaceList.size()).isEqualTo(1);
        Assertions.assertThat(workSpaceList.get(0).getName()).isEqualTo(workSpace.getName());
        verify(mockDao).getEntityList();
    }

    @Test
    public void should_return_workspace_list_when_query_by_userId(WorkSpaceDao mockDao) {
        String userId = "4ff410a897ac21319cf81011";

        List<WorkSpace> workSpaces = Lists.newArrayList();
        WorkSpace workSpace = new WorkSpace();
        workSpace.setName("text");
        ObjectId id = new ObjectId();
        workSpace.setId(id);
        workSpace.setUserId(userId);
        workSpaces.add(workSpace);

        when(mockDao.getEntityListByUserId(userId)).thenReturn(workSpaces);
        List<WorkSpaceDTO> workSpaceList = workSpaceService.getCurrentWorkSpaceListByUserId(userId);
        Assert.assertNotNull(workSpaceList);
        Assertions.assertThat(workSpaceList.size()).isEqualTo(1);
        Assertions.assertThat(workSpaceList.get(0).getName()).isEqualTo(workSpace.getName());
        verify(mockDao).getEntityListByUserId(userId);
    }

    @Test
    public void should_get_workspace_By_id(WorkSpaceDao mockDao) {
        WorkSpace workSpace = new WorkSpace();
        ObjectId id = new ObjectId();
        workSpace.setId(id);
        workSpace.setName("test");
        when(mockDao.findOne("id", id)).thenReturn(workSpace);

        WorkSpace workSpace1 = workSpaceService.getWorkSpaceById(id.toString());
        assertThat(workSpace1).isNotNull();
        assertThat(workSpace1.getName()).isEqualTo("test");
        verify(mockDao).findOne("id", id);
    }
}
