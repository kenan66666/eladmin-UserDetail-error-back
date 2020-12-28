package me.zhengjie.utils.result;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @描述：相应结果类
 * @作者：韦
 * @时间：2019/8/17
 */
@ApiModel("响应结果")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    @ApiModelProperty("响应是否成功")
    private boolean success;
    @ApiModelProperty("响应编码")
    private String code;
    @ApiModelProperty("响应消息")
    private String message;
    @ApiModelProperty("响应数据")
    private T data;


    public Result(String code, String message) {
        this.code = code;
        this.message = message;
        this.success = code.startsWith("0");
    }

    public Result(ResultEnum resultEnum) {
        this(resultEnum.getCode(), resultEnum.getMessage());
    }


    public Result(T data) {
        this(ResultEnum.SUCCESS, data);
    }

    public Result(ResultEnum resultEnum, T data) {
        this(resultEnum);
        this.data = data;
    }

    public Result(String code, String message, T data) {
        this(code, message);
        this.data = data;
    }

    public Result reset(ResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.success = code.startsWith("0");
        this.message = resultEnum.getMessage();
        return this;
    }

    @JsonIgnore
    public boolean isNotSuccess() {
        return !success;
    }

    public static <R> Result<R> copyMessage(Result sourceResult) {
        return new Result<>(sourceResult.getCode(), sourceResult.getMessage());
    }

}
