package com.zj.auth.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.zj.auth.entity.Constants;
import com.zj.auth.entity.UserSession;
import com.zj.domain.entity.bo.auth.ResourceBO;
import com.zj.domain.repository.auth.IResourceRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PermissionService {

    LoadingCache<String, List<ResourceBO>> cacheLoader = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<String, List<ResourceBO>>() {
                @Override
                public List<ResourceBO> load(String userId) {
                    return resourceRepository.getResourceByUserId(userId);
                }
            });

    private final List<String> whiteList = new ArrayList<>();

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private final IResourceRepository resourceRepository;

    public PermissionService(IResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
        whiteList.add(Constants.USER_LOGIN_URL);
        whiteList.add(Constants.USER_LOGOUT_URL);
        whiteList.add("/");
        whiteList.add("/logo.svg");
        whiteList.add("/login");
        whiteList.add("/v1/devops/system/version");
        whiteList.add("/v1/devops/pipeline/web/hook");
        whiteList.add("/css/**");
        whiteList.add("/js/**");
        whiteList.add("/img/**");
        whiteList.add("/fonts/**");
        whiteList.add("/media/**");
        whiteList.add( "/static/**");
    }

    public void removeUserCache(String userId) {
        cacheLoader.invalidate(userId);
    }

    public void removeAllAuthCache() {
        cacheLoader.invalidateAll();
    }

    public boolean isInWhiteList(String uri) {
        return whiteList.stream().anyMatch(whiteUri -> antPathMatcher.match(whiteUri, uri));
    }

    public boolean isLoginUrl(String uri) {
        return Objects.equals(Constants.USER_LOGIN_URL, uri);
    }

    public boolean authCheck(HttpServletRequest request, UserSession userSession) {
        if (Objects.isNull(userSession)) {
            return false;
        }

        String requestUri = request.getRequestURI();
        if (whiteList.contains(requestUri)) {
            log.info("api is in white list={}", requestUri);
            return true;
        }

        List<ResourceBO> userResources = getResourcesFromCache(userSession.getUserId());
        String groupId = userSession.getUserBO().getGroupId();
        List<ResourceBO> groupResources = getResourcesFromCache(groupId);
        Collection<ResourceBO> resources = CollectionUtils.union(userResources, groupResources);
        resources.addAll(groupResources);
        if (CollectionUtils.isEmpty(resources)) {
            return false;
        }

        // 如果在用户拥有的资源列表中未匹配请求的url和HTTP方法，就代表没有权限
        String operate = request.getMethod();
        boolean matchURI = resources.stream().anyMatch(resource -> {
            boolean match = antPathMatcher.match(resource.getContent(), requestUri);
            return match && (Objects.equals(resource.getOperate(), Constants.ANY_OPERATE) || Objects.equals(operate,
                    resource.getOperate()));
        });
        if (!matchURI) {
            log.info("user do not have permission user={} uri={} operate={}", userSession.getUserId(), requestUri,
                    operate);
        }
        return matchURI;
    }

    private List<ResourceBO> getResourcesFromCache(String userId) {
        return cacheLoader.getUnchecked(userId);
    }
}
