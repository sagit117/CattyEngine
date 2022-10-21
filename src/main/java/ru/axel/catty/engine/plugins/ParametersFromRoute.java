package ru.axel.catty.engine.plugins;

import org.jetbrains.annotations.NotNull;
import ru.axel.catty.engine.request.IHttpCattyRequest;
import ru.axel.catty.engine.response.IHttpCattyResponse;
import ru.axel.catty.engine.routing.ICattyRoute;
import ru.axel.catty.engine.routing.RouteExecute;
import ru.axel.catty.engine.utilites.RegexPatterns;

import java.util.regex.Matcher;

public class ParametersFromRoute implements RouteExecute {
    @Override
    public void exec(
        @NotNull IHttpCattyRequest request,
        IHttpCattyResponse response
    ) throws NullPointerException {
        final ICattyRoute route = request.getRoute().orElseThrow();
        final String[] splitRoute = route.getPath().split("/");
        final Matcher matcherAllRouteString = RegexPatterns.getParametersFromRoutePath(route.getPath());

        if (matcherAllRouteString.find()) {
            int index = 0;
            for (String part : splitRoute) {
                final Matcher matcher = RegexPatterns.getParametersFromRoutePath(part);

                if (matcher.find()) {
                    final String paramsName = matcher.group(1);
                    final String paramsValue = request.getPath().split("/")[index];

                    request.setParams(paramsName, paramsValue);
                }

                index++;
            }
        }
    }
}
