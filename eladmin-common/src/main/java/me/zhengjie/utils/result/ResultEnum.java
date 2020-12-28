package me.zhengjie.utils.result;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 响应结果枚举类
 * <p>
 * 响应码保持6位长度
 * 从响应码的首位可以看得出结果类型
 * 首位为：0 成功消息 success 为 true
 * 首位为：1 失败消息 success 为 false
 * 首位为：2 警告消息 success 为 false
 * 首位为：3 提示消息 success 为 false
 *
 * </p>
 *
 * @作者：韦
 * @时间：2019/8/17
 */
@Getter
@ToString
@AllArgsConstructor
public enum ResultEnum {

    // 0 ok/success
    SUCCESS("000000", "成功"),
    LOGIN_SUCCESS("000001", "登录成功"),
    LOGOUT_SUCCESS("000002", "登出成功"),
    DELETE_SUCCESS("000003", "删除成功"),
    INSERT_SUCCESS("000004", "添加成功"),
    UPDATE_SUCCESS("000005", "更新成功"),
    SELECT_SUCCESS("000006", "查询成功"),
    CART_CREATED_SUCCESS("000007", "加入购物车成功"),


    // 1 failed/error
    FAILED("100000", "失败"),
    PARAMS_ERROR("100001", "参数错误"),
    PARAMS_MUST("100001", "参数是必传项"),
    LOGIN_TIME_TOU("100002", "会话超时，请重新登录"),
    LOGIN_FAILED("100003", "登录失败，账号或密码错误"),
    LOGIN_CAPTCHA_FAILED("100004", "验证码错误"),
    SELECT_FAILED("100006", "查询失败"),
    SELECT_LIST_OF_SALES_LEADERBOARDS_FAILED("100007", "获取实销排行榜列表数据异常。"),
    SELECT_PRODUCT_CALENDAR_FAILED("100008", "生产日历没有设置对应的周次"),
    REPEAT_DATA("100007", "该指标序号或者指标名称已存在，请确认。"),
    BSC_FORMULA_FAILED("100008", "公式有误"),
    BSC_DOWNLOAD_TOKEN_EXPIRED("100009", "下载地址已失效，请刷新页面后重试。"),
    BSC_DOWNLOAD_FILE_NOT_FOUND("100010", "您要下载的文件找不到。"),
    BSC_UPLOAD_FAILED("100011", "文件上传异常，请联系管理员。"),
    BSC_UPLOAD_NO_SUPPORTED_FILE_TYPE("100012", "文件上传异常，不支持的文件类型。"),
    DEP_REPEAT_DATA("1000013", "该部门编号或者部门名称已存在，请确认。"),

    CHECK_OA_USER_FAILED("102001", "校验OA系统账号失败"),
    BIND_OA_USER_FAILED("102002", "绑定OA系统账号发生错误"),
    NOT_BIND_OA_USER("102003", "未绑定OA系统账号"),

    //    |-----API Call
    API_CALL_CONTEXT_DEFICIENCY("101004", "API调用的上下文缺失，无法调用API"),
    API_CALL_RESULT_RESOLVING_FAILED("101005", "API调用结果解析失败"),
    API_CALL_FAILED("101006", "API调用失败"),
    API_CALL_RESULT_SERIALIZABLE_FAILED("101007", "API响应结果序列化异常"),

    HTTP_CODE_400("100400", "Bad Request"),
    HTTP_CODE_401("100401", "未授权"),
    HTTP_CODE_404("100404", "找不到页面"),
    HTTP_CODE_403("100403", "访问受限"),
    HTTP_CODE_500("100500", "系统发生未知异常，请及时联系管理员"),


    // 2 WARNING
    LOGIN_USERNAME_WARNING("200001", "用户名不能为空"),
    LOGIN_PASSWORD_WARNING("200002", "密码不能为空"),
    LOGIN_CAPTCHA_WARNING("200003", "验证码不能为空"),
    BSC_YEAR_NOT_FOUND("200004", "BSC年度没找到"),
    BSC_INDEX_NOT_FOUND("200005", "BSC指标没找到"),
    BSC_ACTION_PLAN_NOT_FOUND("200006", "BSC行动方案没找到"),
    TREE_CATALOG_NOT_FOUND("200007", "树状目录没有找到"),

    // 3 info
    CART_EXIST("300001", "商品已经在购物车");

    public static ResultEnum findByCode(String code) {
        ResultEnum[] values = values();
        for (int i = 0; i < values.length; i++) {
            if (values[i].getCode().equals(code))
                return values[i];
        }
        return null;
    }

    private String code;
    private String message;
}
