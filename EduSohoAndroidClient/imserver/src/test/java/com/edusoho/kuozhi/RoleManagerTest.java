package com.edusoho.kuozhi;


import com.edusoho.kuozhi.imserver.BuildConfig;
import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.entity.Role;
import com.edusoho.kuozhi.imserver.managar.IMRoleManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import java.util.Map;

/**
 * Created by 菊 on 2016/5/18.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RoleManagerTest {

    @Before
    public void setUp() {
        IMClient.getClient().init(ShadowApplication.getInstance().getApplicationContext());
        IMRoleManager roleManager = IMClient.getClient().getRoleManager();
        roleManager.createRole(createRole(1, "test1"));
        roleManager.createRole(createRole(2, "test2"));
        roleManager.createRole(createRole(3, "test3"));
    }

    private Role createRole(int rid, String nickname) {
        Role role = new Role();
        role.setRid(rid);
        role.setType("user");
        role.setNickname(nickname);
        role.setAvatar("avatar");
        return role;
    }

    @Test
    public void testUpdateRole() {
        IMRoleManager roleManager = IMClient.getClient().getRoleManager();
        Role role = roleManager.getRole("user", 2);
        role.setNickname("test2");
        role.setAvatar("avatar2");
        role.setType("course");

        roleManager.updateRole(role);
        role = roleManager.getRole("course", 2);
        Assert.assertEquals("test2", role.getNickname());
        Assert.assertEquals("avatar2", role.getAvatar());
        Assert.assertEquals("course", role.getType());
    }

    @Test
    public void testGetRole() {
        IMRoleManager roleManager = IMClient.getClient().getRoleManager();
        Role role = roleManager.getRole("user", 2);
        Assert.assertNotNull(role);
        Assert.assertEquals(role.getRid(), 2);
    }

    @Test
    public void testGetRoleMap() {
        IMRoleManager roleManager = IMClient.getClient().getRoleManager();
        Map<Integer, Role> role = roleManager.getRoleMap(new int[] { 1, 2 });
        Assert.assertNotNull(role);
        Assert.assertEquals(role.size(), 2);
    }
}
