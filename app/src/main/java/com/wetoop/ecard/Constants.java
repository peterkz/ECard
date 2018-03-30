package com.wetoop.ecard;

/**
 * @author Parck.
 * @date 2017/10/17.
 * @desc
 */

public interface Constants {

    String URI_ROOT_SYNC = "https://wd1107361110gszrxd.wilddogio.com";//"https://ecard.wilddogio.com";
    String URI_SEARCH_CARDS = "/search"; // 搜索路径名片库（公共路径，可读可写）
    String URI_ACCOUNT = "/account"; // 搜索路径联系人库（公共路径，可读可写）
    String URI_PUBLIC_TERMS = "/public_pages/terms"; // 用户协议路径（公共路径，只读）
    String URI_RECOMMENDATIONS = "/public_app_recommendations"; // 发现页面中的推荐路径（公共路径，只读)
    String URI_ABOUT = "/public_pages/about"; // 关于我们路径（公共路径，只读）
    String URI_BUDDIES = "/buddies/";
    String URI_CARDS = "/cards/";
    String URI_EXCHANGE = "/exchange";//交换名片列表/添加/删除
    String URI_INCOMING_LOGS = "/incoming-logs/";//接收添加记录列表
    String URI_OUTGOING_LOGS = "/outgoing-logs/";//发送请求记录列表
    String URI_MESSAGE = "/message/";//消息列表/添加
    String URI_APPS = "/apps/";
    String URI_FEEDBACK = "/feedback/";
    String URI_REPORT = "/report/";
    String URI_CARD_ALL = "/card_all/";
    String URI_SEARCH_PHONE = "/search_phone/";
    String URI_SEARCH_ADDRESS = "/search_address/";
    String URI_CONTACT_UPDATE = "/contact_update/";
    String URI_CONTACT_UPDATE_ITEM = "/contact_update_item/";
    String URI_CONTACT_US = "/contact_us/";

    String CURRENT_USER = "CURRENT_USER";
}
