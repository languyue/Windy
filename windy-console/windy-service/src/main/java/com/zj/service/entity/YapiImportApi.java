package com.zj.service.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class YapiImportApi {

    private String name;

    private List<YapiApiModel> list;


    @Data
    public static class YapiApiModel{
        private String title;
        private String path;
        private String method;
        @JsonProperty("req_headers")
        private List<HeaderParam> headers;
        @JsonProperty("req_body_other")
        private String requestBody;
        @JsonProperty("res_body")
        private String resBody;
        @JsonProperty("req_params")
        private List<PathParam> pathParams;

        @JsonProperty("req_query")
        private List<QueryParam> queryParams;
    }

    @Data
    public static class QueryParam {
        private String name;
        private String desc;
    }

    @Data
    public static class PathParam {
        private String name;
        private String desc;
    }

    @Data
    public static class HeaderParam{
        private String name;
        private String value;
    }
}
