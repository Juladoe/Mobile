package com.edusoho.kuozhi.imserver.managar;

import android.content.Context;
import android.text.TextUtils;

import com.edusoho.kuozhi.imserver.entity.Role;
import com.edusoho.kuozhi.imserver.helper.impl.RoleDbHelper;
import java.util.Map;

/**
 * Created by 菊 on 2016/5/15.
 */
public class IMRoleManager {

    private Context mContext;

    public IMRoleManager(Context context)
    {
        this.mContext = context;
    }

    public Map<Integer, Role> getRoleMap(int[] rid) {
        return new RoleDbHelper(mContext).getRoleMap(rid);
    }

    public Role getRole(String type, int rid) {
        if (TextUtils.isEmpty(type)) {
            return new Role();
        }
        return new RoleDbHelper(mContext).getRoleByType(type, rid);
    }

    public long createRole(Role role) {
        if (TextUtils.isEmpty(role.getType()) || role.getRid() == 0) {
            return 0;
        }
        RoleDbHelper roleDbHelper = new RoleDbHelper(mContext);
        if (roleDbHelper.getRoleByType(role.getType(), role.getRid()).getRid() != 0) {
            return 0;
        }
        return roleDbHelper.save(role);
    }

    public int updateRole(Role role) {
        return new RoleDbHelper(mContext).update(role);
    }
}
