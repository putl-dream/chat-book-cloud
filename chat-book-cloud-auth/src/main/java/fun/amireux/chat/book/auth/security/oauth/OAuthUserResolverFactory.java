package fun.amireux.chat.book.auth.security.oauth;

import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OAuthUserResolverFactory {

    private final Map<String, OAuthUserResolver> resolverMap;

    public OAuthUserResolverFactory(List<OAuthUserResolver> resolvers) {
        this.resolverMap = resolvers.stream().collect(Collectors.toUnmodifiableMap(
                resolver -> resolver.support().toLowerCase(Locale.ROOT),
                Function.identity()
        ));
    }

    public OAuthUserResolver getResolver(String provider) {
        if (StringUtils.isBlank(provider)) {
            throw new OAuthResolveException("unsupported_provider", "Oauth provider is required");
        }

        OAuthUserResolver resolver = resolverMap.get(provider.toLowerCase(Locale.ROOT));
        if (resolver == null) {
            throw new OAuthResolveException("unsupported_provider", "Unsupported oauth provider: " + provider);
        }
        return resolver;
    }
}
