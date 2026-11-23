package com.zj.pipeline.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import lombok.Data;

/**
 * 仓库信息
 *
 * @author guyuelan
 * @since 2023/7/18
 */
@Data
public class GitlabRepository {

  private Integer id;
  private String name;
  private String path;
  private Permissions permissions;

  @Data
  public static class Permissions {

    @JsonProperty("project_access")
    private Access projectAccess;

    @JsonProperty("group_access")
    private Access groupAccess;

    public boolean checkPermission() {
      if (Objects.nonNull(projectAccess) && projectAccess.getAccessLevel() >= 30) {
        return true;
      }

      return Objects.nonNull(groupAccess) && groupAccess.getAccessLevel() >= 30;
    }
  }

  @Data
  public static class Access {

    private Integer accessLevel;
    private Integer notificationLevel;
  }
}
