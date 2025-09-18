package io.leanddd.module.misc.model;

import io.leanddd.component.data.DictionaryItem;
import io.leanddd.component.meta.Meta;
import io.leanddd.component.meta.Meta.Type;
import io.leanddd.component.meta.MetaEntity;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@MetaEntity(tableName = "t_dictionary")
@Data
@RequiredArgsConstructor
public class DictionaryImpl implements DictionaryItem {
    @Meta(Type.ID)
    private String id;

    @Meta(Type.String)
    private final String type;

    @Meta(Type.String)
    private final String value;

    @Meta(Type.String)
    private final String label;

    @Meta(Type.String)
    private String shortcut;
}

