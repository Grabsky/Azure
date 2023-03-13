package cloud.grabsky.azure.chat.holder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
public final class TagsHolder {

    @Getter(AccessLevel.PUBLIC)
    private final String permission;

    @Getter(AccessLevel.PUBLIC)
    private final TagResolver tags;

}
