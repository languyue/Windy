package com.zj.contract.pipeline;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

/**
 * 把异构的 envParams 报文还原成具体的环境参数对象。
 *
 * <p>字段本身是多态的，但 JSON 里没有类型标记，只能按结构区分：
 * 带 {@code sshIp} 的是 SSH 主机参数，带 {@code apiService} 的是 K8S 集群参数。</p>
 *
 * @author guyuelan
 */
public class EnvAccessParamsDeserializer extends JsonDeserializer<EnvAccessParams> {

  private static final String SSH_IP = "sshIp";
  private static final String API_SERVICE = "apiService";

  @Override
  public EnvAccessParams deserialize(JsonParser parser, DeserializationContext context)
      throws IOException {
    JsonNode node = parser.getCodec().readTree(parser);
    if (node == null || !node.isObject()) {
      return null;
    }
    if (node.has(SSH_IP)) {
      return parser.getCodec().treeToValue(node, SSHParams.class);
    }
    if (node.has(API_SERVICE)) {
      return parser.getCodec().treeToValue(node, K8SAccessParams.class);
    }
    return null;
  }
}
