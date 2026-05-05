package cloud.grabsky.azure.chat;

import net.kyori.adventure.chat.SignedMessage;

import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class MessageInfo {

    @Getter(AccessLevel.PUBLIC)
    private final SignedMessage.Signature signature;

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PUBLIC)
    private @Nullable Long discordMessageId = null;

}
