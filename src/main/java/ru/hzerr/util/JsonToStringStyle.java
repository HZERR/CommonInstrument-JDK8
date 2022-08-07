package ru.hzerr.util;

import org.apache.commons.lang3.builder.ToStringStyle;

@SuppressWarnings("unused")
public class JsonToStringStyle extends ToStringStyle {

    private static final long serialVersionUID = 1L;

    public JsonToStringStyle() {
        this.setUseClassName(true);
        this.setUseIdentityHashCode(false);
        this.setContentStart(" {" + System.lineSeparator() + '\t');
        this.setFieldSeparator(',' + System.lineSeparator() + '\t');
        this.setFieldSeparatorAtStart(false);
        this.setContentEnd(System.lineSeparator() + "}");
    }
}
