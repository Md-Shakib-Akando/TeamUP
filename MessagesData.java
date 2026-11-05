import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dynamic data model for the TeamUp Messages ("Messages") dashboard view.
 * Aligned with landing page Theme tokens from Theme.java.
 * Compatible with Java 8+.
 */
public class MessagesData {

    public static class ChatMessage {
        public String senderName;
        public String senderInitials;
        public Color avatarBg;
        public Color avatarFg;
        public String text;
        public String time;
        public boolean isOutgoing;

        public ChatMessage(String senderName, String senderInitials, Color avatarBg, Color avatarFg,
                           String text, String time, boolean isOutgoing) {
            this.senderName = senderName;
            this.senderInitials = senderInitials;
            this.avatarBg = avatarBg;
            this.avatarFg = avatarFg;
            this.text = text;
            this.time = time;
            this.isOutgoing = isOutgoing;
        }
    }

    public static class Conversation {
        public String id;
        public String title;
        public String subtitle;
        public String initials;
        public Color avatarBg;
        public Color avatarFg;
        public boolean isChannel;
        public int unreadCount;
        public boolean isOnline;
        public String lastTime;
        public String lastPreview;

        public Conversation(String id, String title, String subtitle, String initials,
                            Color avatarBg, Color avatarFg, boolean isChannel,
                            int unreadCount, boolean isOnline, String lastTime, String lastPreview) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
            this.initials = initials;
            this.avatarBg = avatarBg;
            this.avatarFg = avatarFg;
            this.isChannel = isChannel;
            this.unreadCount = unreadCount;
            this.isOnline = isOnline;
            this.lastTime = lastTime;
            this.lastPreview = lastPreview;
        }
    }

    public final List<Conversation> conversations = new ArrayList<Conversation>();
    public final Map<String, List<ChatMessage>> messagesMap = new HashMap<String, List<ChatMessage>>();
    public int activeConvIndex = 0;
    public String inputDraft = "";
    public boolean inputFocused = false;

    public MessagesData() {
        resetToDefault();
    }

    public void resetToDefault() {
        conversations.clear();
        messagesMap.clear();

        // 1. Team Channels (using Theme colors)
        conversations.add(new Conversation(
                "ch_campusconnect", "#campusconnect-general", "5 members · CampusConnect Workspace", "#",
                Theme.PASTEL_PINK, Theme.BRAND_PINK, true, 0, true, "18m ago",
                "Sofia Martinez: Updated event card task on board"
        ));
        conversations.add(new Conversation(
                "ch_greenroute", "#greenroute-frontend", "4 members · GreenRoute Sustainability", "#",
                Theme.PASTEL_MINT, Theme.SUCCESS_GREEN, true, 0, true, "1h ago",
                "Maya Patel: Map API rate limits verified"
        ));

        // 2. Direct Messages (using Theme pastel tints)
        conversations.add(new Conversation(
                "dm_priya", "Priya Shah", "Design Lead · CampusConnect", "PS",
                Theme.PASTEL_VIOLET, Theme.ACCENT_VIOLET, false, 1, true, "18m ago",
                "Hey Alex! Could you check if the padding matches?"
        ));
        conversations.add(new Conversation(
                "dm_daniel", "Daniel Kim", "Backend Dev · CampusConnect", "DK",
                Theme.PASTEL_MINT, Theme.SUCCESS_GREEN, false, 0, true, "42m ago",
                "Completed event API schema and pushed branch"
        ));
        conversations.add(new Conversation(
                "dm_jordan", "Jordan Lee", "Applicant · Frontend Developer", "JL",
                Theme.PASTEL_SKY, Theme.ACCENT_SKY, false, 1, false, "2h ago",
                "Hi Alex, thanks for reviewing my React application!"
        ));
        conversations.add(new Conversation(
                "dm_amara", "Amara Okafor", "Applicant · QA Engineer", "AO",
                Theme.PASTEL_PINK, Theme.BRAND_PINK, false, 1, true, "Yesterday",
                "Shared my Playwright test report for Sprint 1"
        ));

        // Message histories
        List<ChatMessage> ch1Msgs = new ArrayList<ChatMessage>();
        ch1Msgs.add(new ChatMessage("Priya Shah", "PS", Theme.PASTEL_VIOLET, Theme.ACCENT_VIOLET,
                "Good morning team! Just shared the revised sprint 3 design deliverables in Figma.", "10:14 AM", false));
        ch1Msgs.add(new ChatMessage("Daniel Kim", "DK", Theme.PASTEL_MINT, Theme.SUCCESS_GREEN,
                "Thanks Priya! Schema endpoints for /api/v1/events and /api/v1/teams are fully deployed.", "10:28 AM", false));
        ch1Msgs.add(new ChatMessage("Alex Morgan", "AM", Theme.PASTEL_PINK, Theme.BRAND_PINK,
                "Great work both! I'm reviewing the UI components now and testing double-buffering performance.", "10:35 AM", true));
        ch1Msgs.add(new ChatMessage("Sofia Martinez", "SM", Theme.PASTEL_PINK, Theme.BRAND_PINK,
                "Frontend event card layout is connected to Daniel's schema. Ready for review!", "10:41 AM", false));
        ch1Msgs.add(new ChatMessage("Ethan Chen", "EC", Theme.PASTEL_SUNNY, Theme.HIGHLIGHT_WARM,
                "QA regression test suite passed with zero errors on Java 8 bytecode compatibility.", "10:45 AM", false));
        messagesMap.put("ch_campusconnect", ch1Msgs);

        List<ChatMessage> priyaMsgs = new ArrayList<ChatMessage>();
        priyaMsgs.add(new ChatMessage("Priya Shah", "PS", Theme.PASTEL_VIOLET, Theme.ACCENT_VIOLET,
                "Hey Alex! Could you check if the padding on the team members card matches the 16px Dribbble spec?", "11:02 AM", false));
        priyaMsgs.add(new ChatMessage("Alex Morgan", "AM", Theme.PASTEL_PINK, Theme.BRAND_PINK,
                "Checking now! Yes, adjusted slot width and centered the avatar pills perfectly.", "11:08 AM", true));
        priyaMsgs.add(new ChatMessage("Priya Shah", "PS", Theme.PASTEL_VIOLET, Theme.ACCENT_VIOLET,
                "Looks super clean! Also, Jordan Lee applied for the frontend React role. Have you seen his profile?", "11:12 AM", false));
        messagesMap.put("dm_priya", priyaMsgs);

        List<ChatMessage> danielMsgs = new ArrayList<ChatMessage>();
        danielMsgs.add(new ChatMessage("Daniel Kim", "DK", Theme.PASTEL_MINT, Theme.SUCCESS_GREEN,
                "Pushed branch feature/auth-leadstore with synchronized lead storage.", "9:30 AM", false));
        danielMsgs.add(new ChatMessage("Alex Morgan", "AM", Theme.PASTEL_PINK, Theme.BRAND_PINK,
                "Reviewed and merged! Lead validation works reliably with CSV injection guards.", "9:45 AM", true));
        messagesMap.put("dm_daniel", danielMsgs);

        List<ChatMessage> jordanMsgs = new ArrayList<ChatMessage>();
        jordanMsgs.add(new ChatMessage("Jordan Lee", "JL", Theme.PASTEL_SKY, Theme.ACCENT_SKY,
                "Hi Alex, thanks for reviewing my React application! Looking forward to collaborating on CampusConnect.", "2:15 PM", false));
        messagesMap.put("dm_jordan", jordanMsgs);

        List<ChatMessage> amaraMsgs = new ArrayList<ChatMessage>();
        amaraMsgs.add(new ChatMessage("Amara Okafor", "AO", Theme.PASTEL_PINK, Theme.BRAND_PINK,
                "Shared my Playwright test report for Sprint 1. All 24 integration tests passed.", "Yesterday", false));
        messagesMap.put("dm_amara", amaraMsgs);

        List<ChatMessage> grMsgs = new ArrayList<ChatMessage>();
        grMsgs.add(new ChatMessage("Maya Patel", "MP", Theme.PASTEL_SUNNY, Theme.HIGHLIGHT_WARM,
                "Map API rate limits verified for the upcoming campus sustainability demo.", "11:30 AM", false));
        messagesMap.put("ch_greenroute", grMsgs);
    }

    public Conversation getActiveConversation() {
        if (activeConvIndex >= 0 && activeConvIndex < conversations.size()) {
            return conversations.get(activeConvIndex);
        }
        return conversations.get(0);
    }

    public List<ChatMessage> getActiveMessages() {
        Conversation conv = getActiveConversation();
        List<ChatMessage> list = messagesMap.get(conv.id);
        if (list == null) {
            list = new ArrayList<ChatMessage>();
            messagesMap.put(conv.id, list);
        }
        return list;
    }

    public void selectConversation(int index) {
        if (index >= 0 && index < conversations.size()) {
            activeConvIndex = index;
            Conversation conv = conversations.get(index);
            conv.unreadCount = 0;
        }
    }

    public int getTotalUnreadCount() {
        int sum = 0;
        for (Conversation c : conversations) {
            sum += c.unreadCount;
        }
        return sum;
    }

    public void sendCurrentDraft() {
        if (inputDraft == null || inputDraft.trim().isEmpty()) return;
        String text = inputDraft.trim();
        inputDraft = "";

        Conversation conv = getActiveConversation();
        List<ChatMessage> list = getActiveMessages();
        list.add(new ChatMessage("Alex Morgan", "AM", Theme.PASTEL_PINK, Theme.BRAND_PINK,
                text, "Just now", true));
        conv.lastTime = "Just now";
        conv.lastPreview = "Alex Morgan: " + text;
    }
}
