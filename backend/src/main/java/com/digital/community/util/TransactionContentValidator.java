package com.digital.community.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TransactionContentValidator {

    private static final List<Rule> RULES = buildRules();

    private static List<Rule> buildRules() {
        List<Rule> rules = new ArrayList<>();

        rules.add(new Rule("payment",
                List.of(
                        Pattern.compile("收款(码|账号|账户|方式|信息)?"),
                        Pattern.compile("支付宝(账号|账户|号)?"),
                        Pattern.compile("微信(支付|转账|红包|钱包)"),
                        Pattern.compile("转[账帐].{0,6}(给|到|至)"),
                        Pattern.compile("汇款(给|到|至)?"),
                        Pattern.compile("打款(给|到)?"),
                        Pattern.compile("[银]行卡号"),
                        Pattern.compile("(微信|支付宝|paypal)[号账号]"),
                        Pattern.compile("\\d{6,20}[@\\.]\\w+"),
                        Pattern.compile("[账帐]号\\d{5,}")
                ),
                "内容包含收款/支付信息，社区不支持交易相关内容"
        ));

        rules.add(new Rule("wechat",
                List.of(
                        Pattern.compile("加(微|微信|v[信x])"),
                        Pattern.compile("微[信x]号"),
                        Pattern.compile("加[我你他她]微[信x]"),
                        Pattern.compile("私[聊信][加给发]"),
                        Pattern.compile("[加扫]二维码"),
                        Pattern.compile("扫码(加|添加|关注|咨询)"),
                        Pattern.compile("加[我你]qq"),
                        Pattern.compile("qq号"),
                        Pattern.compile("群号"),
                        Pattern.compile("入群(咨询|购买|下单)"),
                        Pattern.compile("(粉丝|福利|下单|购买)群")
                ),
                "内容包含添加联系方式引导，社区不支持私下交易引导"
        ));

        rules.add(new Rule("trade",
                List.of(
                        Pattern.compile("(出售|售卖|出货).{0,10}(价|钱|费|元|块|rmb|¥)"),
                        Pattern.compile("(价格|多少钱|几钱|报价).{0,10}(私|聊|信)"),
                        Pattern.compile("私[聊信](出|卖|售|发货|交易|购买|下单)"),
                        Pattern.compile("(有意|想要|需要).{0,10}私[聊信]"),
                        Pattern.compile("(出|售|卖).{0,6}(二手|闲置|全新|正品)"),
                        Pattern.compile("(二手|闲置|全新|正品).{0,6}(出|售|卖|转)"),
                        Pattern.compile("(代购|代买|代发|代售)"),
                        Pattern.compile("(包邮|发货).{0,6}(联系|私|微信|加)"),
                        Pattern.compile("(下单|订[购货单]|购买链接)"),
                        Pattern.compile("(现货|库存|补货).{0,6}(联系|私|微信|加|价)"),
                        Pattern.compile("(开团|拼团|团购|上车)"),
                        Pattern.compile("(转手|转让|二手交易)")
                ),
                "内容包含交易/售卖相关信息，社区仅支持经验交流"
        ));

        rules.add(new Rule("price",
                List.of(
                        Pattern.compile("(报价|比价|询价|底价|成交价)"),
                        Pattern.compile("(多少钱|什么价|价格多少|价格[是问])"),
                        Pattern.compile("\\d{1,6}[元块].{0,6}(出|卖|售|转|发)"),
                        Pattern.compile("(出|卖|售).{0,10}\\d{1,6}[元块]"),
                        Pattern.compile("[¥￥]\\d{1,6}"),
                        Pattern.compile("\\d{1,6}(rmb|人民币)")
                ),
                "内容包含比价/报价信息，社区不承接交易功能"
        ));

        return rules;
    }

    public static void validate(String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        String normalized = normalize(content);
        for (Rule rule : RULES) {
            for (Pattern pattern : rule.patterns) {
                if (pattern.matcher(normalized).find()) {
                    throw new com.digital.community.exception.TransactionContentException(rule.message);
                }
            }
        }
    }

    private static String normalize(String text) {
        String s = text.toLowerCase();
        s = s.replaceAll("[\\s\\u00A0\\u3000]+", "");
        s = s.replaceAll("[~～\\-—–]+", "");
        s = s.replaceAll("[.,，。！!？?；;：:、]+", "");
        s = s.replaceAll("[()（）\\[\\]【】{}《》<>「」『』]+", "");
        s = s.replaceAll("[*_·・•]+", "");
        return s;
    }

    private static class Rule {
        final String name;
        final List<Pattern> patterns;
        final String message;

        Rule(String name, List<Pattern> patterns, String message) {
            this.name = name;
            this.patterns = patterns;
            this.message = message;
        }
    }
}
