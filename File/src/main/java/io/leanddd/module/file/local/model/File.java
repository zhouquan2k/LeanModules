package io.leanddd.module.file.local.model;

import io.leanddd.component.data.BaseEntity;
import io.leanddd.component.meta.Meta;
import io.leanddd.component.meta.Meta.Type;
import io.leanddd.component.meta.MetaEntity;
import lombok.*;

@MetaEntity(tableName = "t_file")
@EqualsAndHashCode(callSuper = false)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class File extends BaseEntity<File> {
    private static final long serialVersionUID = 1L;
    @Meta(Type.ID)
    private String fileId;
    @Meta(Type.String)
    private String fileName;
    @Meta(Type.String)
    private String path;
    @Meta(Type.Integer)
    private Long size;
    @Meta(value = Type.String, length = 100)
    private String mimeType;
    @Meta(Type.String)
    private String accessUrl;
}
