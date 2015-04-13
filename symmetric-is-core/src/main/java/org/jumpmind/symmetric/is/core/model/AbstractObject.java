package org.jumpmind.symmetric.is.core.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

abstract public class AbstractObject implements Serializable {

    private static final long serialVersionUID = 1L;
    
    String id = UUID.randomUUID().toString();

    Date createTime = new Date();

    String createBy;

    Date lastUpdateTime = new Date();

    String lastUpdateBy;
    
    public AbstractObject() {
    }
    
    abstract public void setName(String name);
    
    abstract public String getName();
    
    public boolean isSettingNameAllowed() {
        return false;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastModifyTime) {
        this.lastUpdateTime = lastModifyTime;
    }

    public String getLastUpdateBy() {
        return lastUpdateBy;
    }

    public void setLastUpdateBy(String lastModifyBy) {
        this.lastUpdateBy = lastModifyBy;
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractObject && obj.getClass().equals(getClass())) {
            return id.equals(((AbstractObject)obj).getId());
        } else {
            return super.equals(obj);
        }            
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
