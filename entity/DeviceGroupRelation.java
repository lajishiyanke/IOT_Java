package com.iot.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("device_group_relations")
public class DeviceGroupRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("device_id")
    private Long deviceId;
    
    @TableField("group_id")
    private Long groupId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
} 