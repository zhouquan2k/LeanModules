package io.leanddd.module.misc;

import io.leanddd.component.common.Util;
import io.leanddd.component.framework.Context;
import io.leanddd.component.framework.MetadataProvider;
import io.leanddd.component.meta.Metadata;
import io.leanddd.component.meta.Service;
import io.leanddd.module.misc.api.MiscService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.inject.Named;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

@Named
@RequiredArgsConstructor
@Service(type = Service.Type.Mixed, name = "misc", order = -1)
public class MiscServiceImpl implements MiscService {

    private final MetadataProvider metadataProvider;
    private final ResourceLoader resourceLoader;

    @Override
    public String getVersion() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:version.txt");
        return (resource.exists()) ? new String(resource.getInputStream().readAllBytes()) : "";
    }
    
    @Override
    public Object testDate(TestDate date) {

        var curDate = Context.getCurrentDate();
        var dateFormat1 = new SimpleDateFormat(Util.datetimeFormatterString);
        dateFormat1.setTimeZone(TimeZone.getTimeZone("UTC"));
        var dateFormat2 = new SimpleDateFormat(Util.datetimeFormatterString);
        dateFormat2.setTimeZone(TimeZone.getTimeZone(Context.getTimezone()));
        return Map.of("* currentDate", curDate,
                "currentDateStr UTC", dateFormat1.format(curDate),
                "currentDateStr Local", dateFormat2.format(curDate),
                "* inputDate", date.inputDate,
                "inputDateStr UTC", dateFormat1.format(date.inputDate),
                "inputDateStr Local", dateFormat2.format(date.inputDate),
                "* inputDate2", date.inputDate2,
                "TZ", Context.getTimezone());
    }

    @Override
    public void testException() {
        Util.check(false, "check failure");
    }

    @Override
    public Metadata.EntityDef getEntityMetadata(String entityName) {
        return metadataProvider.getEntityDef(entityName);
    }
}

