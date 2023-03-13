package cloud.grabsky.azure.chat.holder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
public final class FormatHolder {

    @Getter(AccessLevel.PUBLIC)
    private final String group;

    @Getter(AccessLevel.PUBLIC)
    private final String format;

}
