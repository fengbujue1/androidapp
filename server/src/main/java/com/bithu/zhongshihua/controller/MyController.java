package com.bithu.zhongshihua.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bithu.zhongshihua.dto.Result;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bithu.zhongshihua.dto.Constants.*;

@RequestMapping
@RestController
@Slf4j
@RequiredArgsConstructor
public class MyController {


    OpenAiClient openAiClient = OpenAiClient.builder()
            .apiHost("https://aihubmix.com/")
            .apiKey(Arrays.asList("sk-zzmbeTwHgXjlKAA4E9A28a644b2d4563BcBd6992A2148646"))
            .build();



    @PostMapping("genDoc")
    public Result genDoc(@RequestBody JSONObject jsonObject) {
        log.error(jsonObject.getString("record"));
        Message system = Message.builder().role(Message.Role.SYSTEM).content(systemText).build();
        String string = jsonObject.getString("record");
        System.out.println(string.length());
        if (string.length() > 10000) {
            string = string.substring(0, 10000);
            System.out.println("substring:" + string);
        }
        Message record = Message.builder().role(Message.Role.USER).content(string).build();
        StringBuilder stringBuffer = new StringBuilder();
        ChatCompletion chatCompletion = ChatCompletion.builder().model("gpt-4o-mini").messages(Arrays.asList(system, record)).build();
        ChatCompletionResponse chatCompletionResponse = openAiClient.chatCompletion(chatCompletion);
        String content = chatCompletionResponse.getChoices().get(0).getMessage().getContent();
        System.out.println("content1:" + content);
        content = content.replaceAll("```json", "");
        content = content.replaceAll("```", "");
        content = content.replaceAll("'", "\"");
        content = extractJson(content);
        System.out.println("content:" + content);
        try {
            JSONObject jsonObject1 = JSON.parseObject(content);
            System.out.println("jsonObject1" + jsonObject1.toJSONString());
            for (String s : jsonObject1.keySet()) {
                jsonObject.put(s, jsonObject1.get(s));
            }

            System.out.println(jsonObject1);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, null);
        }
        System.out.println("jsonObject" + jsonObject.toJSONString());
        String path = getDocPath(jsonObject);
        stringBuffer.append(path);
        return new Result(true, stringBuffer.toString());
//        return new Result(true, "");
    }

    public static String extractJson(String input) {
        List<String> jsonList = new ArrayList<>();

        // 定义正则表达式来匹配JSON格式，支持换行
        String jsonRegex = "\\{[^}]*\\}";

        // 创建模式和匹配器
        Pattern pattern = Pattern.compile(jsonRegex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);

        // 查找并提取JSON
        while (matcher.find()) {
            jsonList.add(matcher.group());
        }

        return jsonList.get(0);
    }

    public static void addRow(String s, XWPFParagraph paragraph) {
        addRow(s, paragraph, false);
    }

    public static void addRow(String s, XWPFParagraph paragraph, boolean isBold) {
        addRow(s, paragraph, isBold, true);
    }

    public static void addRow(String s, XWPFParagraph paragraph, boolean isBold, boolean isBreak) {
        XWPFRun run = paragraph.createRun();
        run.setText(s);
        run.setBold(isBold);
        if (isBreak) {
            run.addBreak();
        }
    }

    public static String getDocPath(JSONObject jsonObject) {
        XWPFDocument document1 = new XWPFDocument();

        String zwjndr1 = jsonObject.getString("zwjndr1");
        String swpzdr1 = jsonObject.getString("swpzdr1");
        System.out.println("1");
        jsonObject.put("zwjndr2", new BigDecimal(zwjndr1.replaceAll("%", "")).compareTo(new BigDecimal(50)) >= 0 ? "针对该纬度谈话识别率较高，结果“建议参考”" : "针对该纬度谈话识别率较高，结果“仅供参考”");
        jsonObject.put("zwjndr3", new BigDecimal(zwjndr1.replaceAll("%", "")).compareTo(new BigDecimal(50)) >= 0 ? "建议参考" : "仅供参考");
        jsonObject.put("swpzdr2", new BigDecimal(swpzdr1.replaceAll("%", "")).compareTo(new BigDecimal(50)) >= 0 ? "针对该纬度谈话识别率较高，结果“建议参考”" : "针对该纬度谈话识别率较高，结果“仅供参考”");
        jsonObject.put("swpzdr3", new BigDecimal(swpzdr1.replaceAll("%", "")).compareTo(new BigDecimal(50)) >= 0 ? "建议参考" : "仅供参考");
        jsonObject.put("yytzfx", strings.get(new Random().nextInt(strings.size())));
        jsonObject.put("glfx", strings1.get(new Random().nextInt(strings1.size())));
        System.out.println("2");
        Integer duration = jsonObject.getInteger("duration");
        int zwjndl = jsonObject.getInteger("zwjndl") == null ? 0 : jsonObject.getInteger("zwjndl");
        int swpzdl = jsonObject.getInteger("swpzdl") == null ? 0 : jsonObject.getInteger("swpzdl");
        System.out.println("3");
        XWPFParagraph xllhx = document1.createParagraph();
        XWPFParagraph gtjh = document1.createParagraph();
        jsonObject.put("xllhx", xllhx);
        jsonObject.put("gtjh", gtjh);
        XWPFParagraph paragraph = document1.createParagraph();
        addRow("1、核心任务", paragraph, true);
        if (zwjndl < 0) {
            jsonObject.put("zwjnddxjg", "初级");
            jsonObject.put("zwjnddxjg4", "初级自我接纳度");
            jsonObject.put("zwjndjy", "初级自我接纳度：该员工在自我接纳度上的评分较低，这可能表示你在日常生活和工作中对自己的评价过于严苛，对自身的情绪体验和挫败感可能感到困扰和不安。这种情况可能会让你在工作中对自己产生过度压力，影响你的工作效率和生活质量。建议你考虑参与一些专业的心理咨询，以帮助你学习如何更好地接纳自己、理解自己的情绪和需求。此外，在工作中，试着为自己设定一些可达到的目标，不要过度期望自己，尝试发现和培养你的优点和特长，用更加积极的方式去面对工作和生活中的挑战。");
            addRow("1. 该员工在日常生活和工作中对自己的评价可能过于严苛，对自身的情绪体验和挫败感可能感到困扰和不安。", xllhx);
            addRow("1. 建议鼓励该员工进一步培养自我照顾和自我关爱的习惯，例如可以利用午休时间做一些瑜伽、冥想或者阅读等放松心情的活动，以增强对自己的接纳和理解。", gtjh);
            addRow("（1）请优先解决其自我否定的惯性，可通过心理咨询行等专业支持+多多鼓励其已有的可见小进步建立其基本信心。", paragraph, true);
        } else if (zwjndl == 0) {
            jsonObject.put("zwjnddxjg", "良好");
            jsonObject.put("zwjnddxjg4", "良好自我接纳度");
            jsonObject.put("zwjndjy", "良好自我接纳度：该员工在自我接纳度上的评分一般，你可能在面对工作压力和生活挑战时，对自己的情绪和需求有一定的理解和接纳，但可能还有改进的空间。建议你在日常工作和生活中进一步培养自我照顾和自我关爱的习惯，例如可以利用午休时间做一些瑜伽、冥想或者阅读等放松心情的活动，以增强对自己的接纳和理解。在工作中，遇到问题和挫折时，记得给自己一些耐心和空间，尝试理解和接纳自己的情绪，不要过度压抑自己。");
            addRow("1. 该员工对自己的情绪和需求有一定的理解和接纳，但可能还有改进的空间。", xllhx);
            addRow("1. 建议鼓励该员工进一步培养自我照顾和自我关爱的习惯，例如可以利用午休时间做一些瑜伽、冥想或者阅读等放松心情的活动，以增强对自己的接纳和理解。", gtjh);
            addRow("（1）请鼓励其保持并强化自我调节能力，以便预防压力反弹转化为自我否定。", paragraph, true);
        } else {
            jsonObject.put("zwjnddxjg", "优秀");
            jsonObject.put("zwjnddxjg4", "优秀自我接纳度");
            jsonObject.put("zwjndjy", "优秀自我接纳度：该员工在自我接纳度上的评分较高，表示你能很好地理解和接纳自己的情绪和需求，这是非常值得赞赏的。在这个基础上，你可以进一步将这种接纳转化为更具实际效果的行动，例如在日常工作中，通过有效的时间管理和自我照顾，以保持良好的情绪和工作效率，提升工作和生活质量。");
            addRow("1. 该员工能很好地理解和接纳自己的情绪和需求。", xllhx);
            addRow("1.建议多加赞赏该员工对自身情绪需求的理解和接纳能力，能更好达到敞开心扉，建立信任和深入共情的效果。", gtjh);
            addRow("（1）可优先考虑协助其将自我接纳度上的优势转化为团队价值，防止优势能力被埋没。", paragraph, true);
        }
        if (swpzdl < 0) {
            jsonObject.put("zwpzdjg", "初级");
            jsonObject.put("zwpzdjg4", "初级社会适应性");
            jsonObject.put("swpzdjy", "初级社会适应性：该员工的思想偏执度较高，这可能意味着你在工作或人际交往中，可能存在对他人持有怀疑、不信任或不理解的态度。这种情况可能会影响你的团队合作效果和职场关系。建议你尝试进行一些社交技巧的训练，以提高你的人际交往和沟通能力。在工作中，更加开放地接受他人的观点和建议，尝试理解和尊重他人的工作方式，这将有助于你在工作中建立更和谐的关系，提升团队合作效果。");
            addRow("2. 该员工在工作或人际交往中，可能存在对他人持有怀疑、不信任或不理解的态度。", xllhx);
            addRow("2. 建议鼓励员工更开放地接受他人的观点和建议，多多尝试理解和尊重他人的工作方式，比如咱们可以这样引导：“您刚才主动理解同事想法的做法特别好，如果在日常工作中多留意别人的协作需求，相信团队配合会更顺畅。”或者，“我理解你的想法，那你也愿意听听别人的想法和观点吗？”。", gtjh);
            addRow("（2）请优先先切断其内心的\"怀疑-冲突-孤立\"恶性循环，别让无效猜疑变成职场绊脚石。", paragraph, true);
        } else if (swpzdl == 0) {
            jsonObject.put("zwpzdjg", "良好");
            jsonObject.put("zwpzdjg4", "良好社会适应性");
            jsonObject.put("swpzdjy", "良好社会适应性：该员工的思想偏执度一般，你在面对他人和处理人际关系时，可能有时会感到困惑或矛盾。在与同事的互动和团队合作中，你可能时而感到接纳和理解，时而感到怀疑和不安。建议你在工作中，更多地尝试理解和接纳他人的观点和感受，这样可以帮助你更好地与他人建立联系，提高你的团队合作能力，从而提升工作效率。");
            addRow("2. 该员工在面对他人和处理人际关系时，可能有时会感到困惑或矛盾。", xllhx);
            addRow("2. 建议鼓励员工多多保持稳定的社交状态，比如首先可以引导其想像自己身边有一个情绪时而稳定，时而大起大落的朋友；再次，通过引导换位思考来让其理解社交协作中“稳定”和“可预测”的重要性；最后，着重表扬并鼓励其保持情绪稳定状态时期的表现，比如，“上次与你谈话之后，你两个月都没有向同事发过脾气了，值得表扬！”。", gtjh);
            addRow("（2）请鼓励并指导其把飘忽不定的社交状态焊死在「靠谱模式」，让其保持「可预测」的协作节奏，预防社交状态大起大落。", paragraph, true);
        } else {
            jsonObject.put("zwpzdjg", "优秀");
            jsonObject.put("zwpzdjg4", "优秀社会适应性");
            jsonObject.put("swpzdjy", "优秀社会适应性：该员工的思想偏执度较低，你在与他人的交往和合作中，通常能理解、接纳和尊重他人，这是非常值得赞赏的。在这个基础上，你可以进一步将这种理解和接纳转化为具体的行动，例如在工作中，积极参与团队合作，主动听取和接纳他人的建议，共同解决工作中的问题，这将有助于提升你的团队合作效果和工作效率。");
            addRow("2. 该员工在与他人的交往和合作中，通常能理解、接纳和尊重他人。", xllhx);
            addRow("2. 建议多加赞赏员工理解、接纳和尊重他人的能力，但同时也要注意询问其是否存在“人情负担”过重的情况，如果存在，可以引导其展开谈话并向上反应，比如“如果你帮忙调解别人之间的问题超过两次都没能有效解决，就一定要向上反应，请相关的领导出面调解工作问题，请不要把你自己陷入得太深啦，以免影响你自己的工作。”", gtjh);
            addRow("（2）可优先考虑协助其将自我在人际敏感度上的优势转化为团队资源，兑换成团队战斗力，警惕过度自我消耗。", paragraph, true);
        }
        addRow("2、指导性建议总述", paragraph, true);
        if (zwjndl < 0) {
            addRow("（1）该员工可能长期处于持续自我怀疑状态，需先通过专业心理咨询处理情绪根源，再配合分阶段目标管理让员工直观看到能力提升。条件允许的情况下，可以考虑避免重点且过高工作压力，从健康习惯培养和生活平衡切入重建稳定状态。", paragraph, false);
        } else if (zwjndl == 0) {
            addRow("（1）该员工具有较强自我驱动能力，建议可提供资源倾斜（如创新项目主导权、跨部门协作机会），通过承担重点任务或组织优秀经验分享会来实现个人价值外溢，影响并帮助团队其他人，同时用领导力培训防止过度自我消耗。", paragraph, false);
        } else {
            addRow("（1）该员工具有较强自我驱动能力，建议可提供资源倾斜（如创新项目主导权、跨部门协作机会），通过承担重点任务或组织优秀经验分享会来实现个人价值外溢，影响并帮助团队其他人，同时用领导力培训防止过度自我消耗。", paragraph, false);
        }
        if (swpzdl < 0) {
            addRow("（2）该员工可能戴着「总有刁民想害朕」的滤镜看职场，得先通过心理咨询或专业的思政工作谈话卸下其最外层的心理防御，再用角色扮演训练等方式破除社交恐惧。初期给明确指令和固定搭档降低沟通压力，别急着塞进复杂协作项目，先稳住基本盘再慢慢拓展。", paragraph, false);
        } else if (swpzdl == 0) {
            addRow("（2）该员工在团队协作中，可能时而积极投入，时而回避沟通，建议采用「情绪温度计」每日自评（简单勾选协作信心值），搭配沟通话术模板化解常见冲突场景。可优先安排与性格稳定的老员工搭档，通过可量化的协作任务（如每周完成3次跨岗求助等）逐步固化正向经验。", paragraph, false);
        } else {
            addRow("（2）该员工可能非常擅长化解矛盾，但同时也易陷入人情负担，建议协助其通过担任跨部门协调人、新人沟通培训师等角色，将其洞察力导入组织系统，转化为系统性解决方案。重要会议可安排其观察团队动态并输出沟通效率报告，用机制化设计，比如当调解未果时，必须移交上级处理，避免个人过度卷入，避免能力的滥用和内耗。", paragraph, false);
        }
        addRow("3、工作建议细则", paragraph, true);
        addRow("(1)\t自我接纳度（经验性回避）", paragraph, true);
        if (zwjndl < 0) {
            addRow("①　心理健康支持：建议定期安排与心理专家进行一对一的咨询，这些咨询可以帮助员工识别自我批评的源头，并发展出更加同情自己的态度。心理咨询可以包括认知行为疗法（CBT）和接纳承诺疗法（ACT）等技术，以帮助员工理解并改善他们对自己的看法。", paragraph, false);
            addRow("②　工作坊和讲座：组织工作坊和讲座，教授自我接纳的重要性及其对个人福祉和工作表现的影响。这些活动可以涵盖自我关怀的实践、压力管理技巧和建立积极自我形象的策略。", paragraph, false);
            addRow("③　工作目标设置：与员工一起制定SMART目标——具体、可衡量、可实现、相关性强、时限性的（SMART）目标。这些目标应当挑战员工，但同时也要确保它们是现实可行的，以减少员工的焦虑和压力。", paragraph, false);
            addRow("④　进度跟踪和反馈：提供一个透明的目标跟踪系统，定期与员工检查进度，并提供积极的反馈和必要的调整建议。这可以帮助员工看到他们的进步，增强他们的成就感和自我效能感。", paragraph, false);
            addRow("⑤　优点和特长发展：进行个人优势评估，识别员工的天赋和技能，以及他们在职场上的潜力。根据个人优势评估的结果，为员工设计个性化的职业发展计划。这些计划可以包括职业指导、技能提升培训和参与特定项目的机会，以促进个人优势的进一步发展。", paragraph, false);
            addRow("⑥　工作生活平衡：提倡工作与生活的平衡，鼓励员工合理安排工作，灵活工作安排，帮助员工更好地平衡工作和个人生活的需求。", paragraph, false);
            addRow("⑦　健康生活宣传：通过组织健康生活方式的活动和竞赛，鼓励员工维持一个健康的生活方式。这些活动可以包括团体运动、健康饮食研讨会和时间管理工作坊，以促进员工的整体福祉。", paragraph, false);
        } else if (zwjndl == 0) {
            addRow("①　自我照顾习惯：建议并鼓励员工在日常生活中实践日常放松练习，如进行短暂的冥想、瑜伽或阅读，以促进心理放松和减轻工作压力。可以通过提供工作场所的冥想角落或组织定期的瑜伽课程来促进这一习惯的养成。", paragraph, false);
            addRow("②　情绪管理培训：提供情绪智力和压力管理的工作坊，帮助员工更好地理解和管理自己的情绪。", paragraph, false);
            addRow("③　自我激励策略：教育员工如何通过积极的自我对话和认可自己的成就来自我激励。", paragraph, false);
            addRow("④　问题解决技能：提供解决问题和决策制定的培训，以加强员工面对挑战时的自信和应对能力。", paragraph, false);
        } else {
            addRow("①　时间管理和效率提升：提供时间管理培训，帮助员工优化日常工作流程，提高工作效率。", paragraph, false);
            addRow("②　创新和创造力培训：鉴于这些员工可能对自己有较好的理解，提供创新思维和创造力工作坊，以利用他们的个人优势", paragraph, false);
            addRow("③　高级职业规划：与员工合作制定更高级的职业发展路径，以实现个人和组织目标的更好对齐。", paragraph, false);
        }
        addRow("(2)\t思想偏执度（认知融合）", paragraph, true);
        if (swpzdl < 0) {
            addRow("①\t深入心理辅导：建议定期安排与心理专家提供长期且定期的个人或团体心理辅导，重点在于挖掘和处理偏执思维的根源，以及建立和强化信任感。专业辅导将帮助员工识别他们的思维模式，学习如何以更积极的方式理解和评价自己及他人。", paragraph, false);
            addRow("②\t工作坊和讲座：建议开展系列工作坊和讲座，旨在提升他们在职场的社交能力和团队互动效果。内容将覆盖社会适应的基本知识、有效沟通技巧、信任建立的策略、多元文化的理解与尊重，以及团队协作的实践。此外，还包括冲突管理、自我意识提升、情绪智力发展，以及行为改变策略，以促进更健康的工作环境和人际关系。", paragraph, false);
            addRow("③\t开展个性化的社交技巧培训：通过角色扮演和模拟日常交往情景的专门培训，帮助员工提高理解别人想法的能力和沟通技巧。这样的训练可以让员工在工作中更好地与人相处，建立良好的人际关系。", paragraph, false);
            addRow("④\t加强团队合作：安排需要大家一起合作解决问题的团队建设活动。这样做可以让员工通过实践学会如何和别人一起工作，以及在团队中如何发挥自己的作用。", paragraph, false);
            addRow("⑤\t多样性和包容性教育：实施多样性和包容性教育计划，以促进工作场所的理解和尊重。这些计划旨在扩大员工对不同背景和观点的认识，同时减少误解和冲突。比如通过培训课程和研讨会，提高员工对不同文化、性别、年龄、性取向、宗教信仰等方面的理解和尊重。这种培训可以包括文化敏感性训练、消除偏见的工作坊，以及学习如何在多元文化环境中有效沟通的课程。", paragraph, false);
            addRow("⑥\t工作生活平衡：提倡工作与生活的平衡，鼓励员工合理安排工作，灵活工作安排，帮助员工更好地平衡工作和个人生活的需求。", paragraph, false);
        } else if (swpzdl == 0) {
            addRow("①　增强情绪智力：为员工提供情绪智力提升课程，帮助他们更好地理解自己的情绪，以及如何在职场中有效地表达和管理这些情绪。这将促进更健康的工作环境和更有效的团队动力。", paragraph, false);
            addRow("②　有效沟通工作坊：通过工作坊和实践研讨会，教授员工如何更有效地沟通和解决冲突。这些活动可以帮助员工学习如何倾听他人，表达自己的观点，以及如何建立共识。", paragraph, false);
            addRow("③　制度和同伴支持：建立制度和同伴支持网络，鼓励员工在工作中相互学习，提供情感支持，共同成长。这样的系统可以帮助员工在工作和人际交往中感到更加自信和舒适。", paragraph, false);
            addRow("④　自我反思和同事反馈：鼓励员工定期进行自我评估，并开放地接受来自同事的反馈。这有助于员工持续地自我提升，并在团队内部建立起一种积极的反馈文化。", paragraph, false);
        } else {
            addRow("①　领导力发展：鉴于这些员工在社交方面的能力较强，可以提供领导力发展项目，帮助他们担任团队领导角色。", paragraph, false);
            addRow("②　高级沟通策略：提供进阶沟通策略培训，以优化团队协作和冲突解决。", paragraph, false);
            addRow("③　跨部门项目参与：鼓励这些员工参与跨部门项目，充分利用他们的社交技能和协作精神。", paragraph, false);
            addRow("④　创新和创意工作坊：利用员工的社会适应性优势，通过创新工作坊促进团队协作和创意思维。", paragraph, false);
        }
        jsonObject.put("xxjxbzfj", paragraph);
        String regex = "\\{([a-zA-Z0-9]+)\\}";

        // 编译正则表达式
        Pattern pattern = Pattern.compile(regex);
        XWPFDocument document;
        try {
            String os = System.getProperty("os.name").toLowerCase();

            InputStream in;
            if (os.contains("win")) {
                in = new FileInputStream("d://template.docx");
            } else {
                in = new FileInputStream("/home/template.docx");
            }
            document = new XWPFDocument(in);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        String[] seriesTitles = {"情绪波动"};
        //x轴
        List<Number[]> values = new ArrayList<>();
        //日处理能力
        List<String> integers = generateList(duration);
        String[] categories = integers.toArray(new String[]{});
        Number[] value1 = generateRandomArray(categories.length);
        values.add(value1);
        XWPFChart xChart = document.getCharts().get(0);//获取第1个图表
        generateChart(xChart, seriesTitles, categories, values);
        List<XWPFTable> xwpfTables = document.getTables();
        List<String> strings2 = Arrays.asList("yytzfx", "record", "glfx", "zwjndjy", "zwjndfz", "swpzdjy", "swpzdfz", "xxjxbzfj");
        List<String> noBreakList = Arrays.asList("zwjndr3", "zwjnddxjg4", "zwpzdjg4", "zwjnddxjg", "zwpzdjg", "zwjndr1", "zwjndr2", "swpzdr1", "swpzdr2");
        List<String> list3 = Arrays.asList("zwjnddxjg", "zwpzdjg");
        for (XWPFTable table : xwpfTables) {
            // 获取表格行
            List<XWPFTableRow> rows = table.getRows();
            for (XWPFTableRow row : rows) {
                // 表格列
                List<XWPFTableCell> tableCells = row.getTableCells();
                for (XWPFTableCell cell : tableCells) {
//                    System.out.println(cell.getText());
                    Matcher matcher = pattern.matcher(cell.getText().trim());
                    // 判断是否符合规则并匹配
                    if (matcher.matches()) {
                        // 获取匹配到的内容
                        cell.removeParagraph(0);
                        String matchedText = matcher.group(1);
                        System.out.println("匹配的内容为1: " + matchedText);
                        Object o = jsonObject.get(matchedText);
                        if (o instanceof XWPFParagraph) {
                            cell.setParagraph((XWPFParagraph) o);
                        } else {
                            // 创建一个新的段落
                            XWPFParagraph paragraph1 = document1.createParagraph();
                            if (!strings2.contains(matchedText)) {
                                paragraph1.setAlignment(ParagraphAlignment.CENTER);
                            } else {
                                paragraph1.setAlignment(ParagraphAlignment.LEFT);
                            }
                            if (!list3.contains(matchedText)) {
                                if (matchedText.equals("record")) {
                                    String text = jsonObject.getOrDefault(matchedText, "无").toString();
                                    String[] split = text.split("\n");
                                    for (String s : split) {
                                        addRow(s, paragraph1);
                                    }
                                } else {
                                    XWPFRun run = paragraph1.createRun();
                                    run.setText(jsonObject.getOrDefault(matchedText, "无").toString());
                                    if (!noBreakList.contains(matchedText)) {
                                        run.addBreak();
                                    }
                                }
                            } else {
                                XWPFRun run = paragraph1.createRun();
                                run.setText(jsonObject.getOrDefault(matchedText, "无").toString());
                                run.setBold(true);
                                XWPFRun run1 = paragraph1.createRun();
                                if (matchedText.equals("zwjnddxjg")) {
                                    run1.setText("自我接纳度");
                                } else {
                                    run1.setText("社会适应性");
                                }
                            }

                            cell.setParagraph(paragraph1);
                        }
                        continue;
                    } else {
//                        System.out.println("输入不符合规则-----" + xwpfRun.toString());
                    }
                    // 列里面的内容
                    for (XWPFParagraph paragraph1 : cell.getParagraphs()) {
                        List<XWPFRun> runs = paragraph1.getRuns();
                        for (XWPFRun xwpfRun : runs) {
                            // 将列里面的$换成
                            matcher = pattern.matcher(xwpfRun.toString());

                            // 判断是否符合规则并匹配
                            if (matcher.matches()) {
                                // 获取匹配到的内容
                                String matchedText = matcher.group(1);
                                System.out.println("匹配的内容为: " + matchedText);
                                xwpfRun.setText(jsonObject.getOrDefault(matchedText, "空").toString(), 0);
                            } else {
//                                System.out.println("输入不符合规则-----" + xwpfRun.toString());
                            }
                        }
                    }
                }
            }
        }
        System.out.println("5");
        String s = UUID.randomUUID().toString();
        System.out.println("6");
        String os = System.getProperty("os.name").toLowerCase();

        String path;
        if (os.contains("win")) {
            path = "d://zshdoc/" + s + ".docx";
        } else {
            path = "/home/zshdoc/" + s + ".docx";
        }
        System.out.println("path:" + path);
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("7");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.write(fos);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("8");
        return "https://biexiaozhi.cn/zshdoc/" + file.getName();
    }

    public static List<String> generateList(int length) {
        List<String> result = new ArrayList<>();

        if (length <= 10) {
            for (int i = 1; i < length; i++) {
                result.add(i + "s");
            }
        } else {
            int avg = length / 10;
            int remainder = length % 10;
            for (int i = 1; i <= 10; i++) {
                result.add(i < remainder ? i * avg + 1 + "s" : i * avg + "s");
            }
        }

        return result;
    }

    public static Number[] generateRandomArray(int length) {
        Number[] randomArray = new Number[length];
        Random rand = new Random();

        for (int i = 0; i < length; i++) {
            randomArray[i] = rand.nextInt(41) + 50; // 50 to 90 (inclusive)
        }

        return randomArray;
    }

    public static void generateChart(XWPFChart chart, String[] series, String[] categories, List<Number[]> values) {
        String chartTitle = "情绪波动图";
        final List<XDDFChartData> data = chart.getChartSeries();//不知道这个ChartSeries代表什么意思
        final XDDFLineChartData line = (XDDFLineChartData) data.get(0);//这里一般获取第一个,我们这里是折线图就是XDDFLineChartData

        final int numOfPoints = categories.length;

        final String categoryDataRange = chart.formatRange(new CellRangeAddress(1, numOfPoints, 0, 0));

        final XDDFDataSource<?> categoriesData = XDDFDataSourcesFactory.fromArray(categories, categoryDataRange, 0);
        for (int i = 0; i < values.size(); i++) {
            final String valuesDataRange = chart.formatRange(new CellRangeAddress(1, numOfPoints, i + 1, i + 1));
            Number[] value = values.get(i);
            final XDDFNumericalDataSource<? extends Number> valuesData = XDDFDataSourcesFactory.fromArray(value, valuesDataRange, i + 1);
            XDDFChartData.Series ser;//图表中的系列
            ser = line.getSeries().get(i);
            ser.replaceData(categoriesData, valuesData);
            CellReference cellReference = chart.setSheetTitle(series[i], 1);//修改系列标题
            ser.setTitle(series[i], cellReference);
        }

        chart.plot(line);
        chart.setTitleText(chartTitle);//折线图标题
        chart.setTitleOverlay(false);
    }
}
