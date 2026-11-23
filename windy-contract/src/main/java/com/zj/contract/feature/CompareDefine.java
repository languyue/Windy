package com.zj.contract.feature;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CompareDefine {
    /**
     * 对比的Key
     * */
    @NotBlank
    private String compareKey;
    /**
     * key转化后的响应的数据。
     *
     * <p>运行期从第三方响应里取出的片段，可能是标量、集合或对象，所以只能是 Object；
     * 它只在 client 的比较过程中存在，不参与序列化，也不落库。</p>
     * */
    @JsonIgnore
    private Object responseValue;
    /**
     * 运算符
     * */
    @NotBlank
    private String operator;

    /**
     * 期待的值
     * */
    @NotBlank
    private String expectValue;
}
