package com.wetoop.ecard.tools;

import com.wetoop.ecard.api.model.Address;
import com.wetoop.ecard.api.model.Email;
import com.wetoop.ecard.api.model.Information;
import com.wetoop.ecard.api.model.Phone;
import com.wetoop.ecard.api.model.Url;
import com.wetoop.ecard.bean.CardBean;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式解析VCard数据
 * Created by User on 2017/10/26.
 */

public class RegularToQR {
    //解析名字
    public static Pattern patternFullName = Pattern.compile("\\nFN:(\\w+)");
    //解析公司
    public static Pattern patternORG = Pattern.compile("\\nORG:(\\w+)");
    //解析职位
    public static Pattern patternTITLE = Pattern.compile("\\nTITLE:(\\w+)");
    //解析备注
    public static Pattern patternNOTE = Pattern.compile("\\nNOTE:(\\w+)");
    //解析手机
    public static Pattern patternTELCELL= Pattern.compile("\\nTEL;CELL;VOICE:(\\w+)");
    //解析工作电话
    public static Pattern patternTELWORK= Pattern.compile("\\nTEL;WORK;VOICE:(\\w+)");
    //解析住宅电话
    public static Pattern patternTELHOME= Pattern.compile("\\nTEL;HOME;VOICE:(\\w+)");
    //解析邮箱
    public static Pattern patternEMAIL= Pattern.compile("\\nEMAIL:(\\w+)");
    //解析网页
    public static Pattern patternURL= Pattern.compile("\\nURL:(\\w+)");
    //解析工作地址
    public static Pattern patternADRWORK= Pattern.compile("\\nADR;WORK:(\\w+)");
    //解析住宅地址
    public static Pattern patternADRHOME= Pattern.compile("\\nADR;HOME:(\\w+)");
    //解析card_id
    public static String patternCardId(String cardId){
        String s1[] = cardId.split("card_id:");
        String s2[] = s1[1].split("\\n");
        return s2[0];
    }
    //创建二维码
    public static String createQRCardStr(CardBean cardBean) {
        String contents = "BEGIN:VCARD\nVERSION:3.0";
        if(cardBean.getInformation().getName() != null) {
            String fnStr = "\nFN:" + cardBean.getInformation().getName();
            contents += fnStr;
        }else{
            String fnStr = "\nFN: ";
            contents += fnStr;
        }
        if(cardBean.getInformation().getName() != null) {
            String titleStr = "\nTITLE:" + cardBean.getInformation().getPosition();
            contents += titleStr;
        }
        if(cardBean.getInformation().getName() != null) {
            String orgStr = "\nORG:"+cardBean.getInformation().getCompany();
            contents += orgStr;
        }
        if(cardBean.getInformation().getNote() != null) {
            String noteStr = "\nNOTE:"+cardBean.getInformation().getNote();
            contents += noteStr;
        }
        String cardIdStr = "\ncard_id:"+cardBean.getInformation().getCard_id();
        contents += cardIdStr;
        if(cardBean.getPhones() != null) {
            for(int i=0;i<cardBean.getPhones().size();i++) {
                if("手机".equals(cardBean.getPhones().get(i).getType())) {
                    String cellPhoneStr = "\nTEL;CELL;VOICE:" + cardBean.getPhones().get(i).getPhone();
                    contents += cellPhoneStr;
                }else if("工作号码".equals(cardBean.getPhones().get(i).getType())){
                    String workPhoneStr = "\nTEL;WORK;VOICE:" + cardBean.getPhones().get(i).getPhone();
                    contents += workPhoneStr;
                }else if("住宅号码".equals(cardBean.getPhones().get(i).getType())){
                    String homePhoneStr = "\nTEL;HOME;VOICE:" + cardBean.getPhones().get(i).getPhone();
                    contents += homePhoneStr;
                }
            }
        }
        if(cardBean.getEmails() != null) {
            for(int i=0;i<cardBean.getEmails().size();i++) {
                String emailStr = "\nEMAIL:" + cardBean.getEmails().get(i).getEmail();
                contents += emailStr;
            }
        }
        if(cardBean.getUrls() != null) {
            for(int i=0;i<cardBean.getUrls().size();i++) {
                String urlStr = "\nURL:" + cardBean.getUrls().get(i).getUrl();
                contents += urlStr;
            }
        }
        if(cardBean.getAddresses() != null){
            for(Address address : cardBean.getAddresses()){
                if("工作地址".equals(address.getType())){
                    String wordAddStr = "\nADR;WORK:"+address.getAddress();
                    contents += wordAddStr;
                }
                if("个人地址".equals(address.getType())){
                    String wordAddStr = "\nADR;HOME:"+address.getAddress();
                    contents += wordAddStr;
                }
            }
        }
        contents += "\nEND:VCARD";
        return contents;
    }

    public static CardBean decodeToCardBean(String rawResult){
        CardBean cardBean = new CardBean();
        Information information = new Information();
        List<Phone> phoneList = new ArrayList<>();
        List<Email> emailList = new ArrayList<>();
        List<Url> urlList = new ArrayList<>();
        List<Address> addressList = new ArrayList<>();
        if(rawResult.indexOf("FN")>0){
            Matcher matcher = RegularToQR.patternFullName.matcher(rawResult);
            if (matcher.find()) {
                information.setName(matcher.group(1));
            }
        }
        if(rawResult.indexOf("TITLE")>0){
            Matcher matcher = RegularToQR.patternTITLE.matcher(rawResult);
            if (matcher.find()) {
                information.setPosition(matcher.group(1));
            }
        }
        if(rawResult.indexOf("ORG")>0){
            Matcher matcher = RegularToQR.patternORG.matcher(rawResult);
            if (matcher.find()) {
                information.setCompany(matcher.group(1));
            }
        }
        if(rawResult.indexOf("NOTE")>0){
            Matcher matcher = RegularToQR.patternNOTE.matcher(rawResult);
            if (matcher.find()) {
                information.setNote(matcher.group(1));
            }
        }
        if(rawResult.indexOf("card_id")>0){
            String cardId = RegularToQR.patternCardId(rawResult);
            if (cardId != null) {
                information.setCard_id(cardId);
            }
        }
        cardBean.setInformation(information);
        if(rawResult.indexOf("TEL;CELL;VOICE")>0){
            Matcher matcher = RegularToQR.patternTELCELL.matcher(rawResult);
            if (matcher.find()) {
                Phone phone = new Phone();
                phone.setPhone(matcher.group(1));
                phone.setType("手机");
                phoneList.add(phone);
            }
        }
        if(rawResult.indexOf("TEL;WORK;VOICE")>0){
            Matcher matcher = RegularToQR.patternTELWORK.matcher(rawResult);
            if (matcher.find()) {
                Phone phone = new Phone();
                phone.setPhone(matcher.group(1));
                phone.setType("工作电话");
                phoneList.add(phone);
            }
        }
        if(rawResult.indexOf("TEL;HOME;VOICE")>0){
            Matcher matcher = RegularToQR.patternTELHOME.matcher(rawResult);
            if (matcher.find()) {
                Phone phone = new Phone();
                phone.setPhone(matcher.group(1));
                phone.setType("住宅电话");
                phoneList.add(phone);
            }
        }
        if(phoneList.size() > 0){
            cardBean.setPhones(phoneList);
        }
        if(rawResult.indexOf("EMAIL")>0){
            Matcher matcher = RegularToQR.patternEMAIL.matcher(rawResult);
            if (matcher.find()) {
                Email email = new Email();
                email.setEmail(matcher.group(1));
                email.setType("工作邮箱");
                emailList.add(email);
            }
        }
        if(emailList.size() > 0){
            cardBean.setEmails(emailList);
        }
        if(rawResult.indexOf("URL")>0){
            Matcher matcher = RegularToQR.patternURL.matcher(rawResult);
            if (matcher.find()) {
                Url url = new Url();
                url.setUrl(matcher.group(1));
                url.setType("工作网页");
                urlList.add(url);
            }
        }
        if(urlList.size() > 0){
            cardBean.setUrls(urlList);
        }
        if(rawResult.indexOf("ADR;WORK")>0){
            Matcher matcher = RegularToQR.patternADRWORK.matcher(rawResult);
            if (matcher.find()) {
                Address address = new Address();
                address.setAddress(matcher.group(1));
                address.setType("工作地址");
                addressList.add(address);
            }
        }
        if(rawResult.indexOf("ADR;HOME")>0){
            Matcher matcher = RegularToQR.patternADRHOME.matcher(rawResult);
            if (matcher.find()) {
                Address address = new Address();
                address.setAddress(matcher.group(1));
                address.setType("住宅地址");
                addressList.add(address);
            }
        }
        if(addressList.size() > 0){
            cardBean.setAddresses(addressList);
        }
        return cardBean;
    }
}
