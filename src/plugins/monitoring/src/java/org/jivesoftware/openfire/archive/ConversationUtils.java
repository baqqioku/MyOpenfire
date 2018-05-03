package org.jivesoftware.openfire.archive;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.plugin.MonitoringPlugin;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.LocaleUtils;
import org.jivesoftware.util.NotFoundException;
import org.jivesoftware.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

public class ConversationUtils {
	private static final Logger Log = LoggerFactory.getLogger(ConversationUtils.class);

	public int getBuildProgress() {
		MonitoringPlugin plugin = (MonitoringPlugin) XMPPServer.getInstance().getPluginManager()
				.getPlugin("monitoring");

		ArchiveIndexer archiveIndexer = (ArchiveIndexer) plugin.getModule(ArchiveIndexer.class);

		Future<Integer> future = archiveIndexer.getIndexRebuildProgress();
		if (future != null) {
			try {
				return ((Integer) future.get()).intValue();
			} catch (Exception e) {
				Log.error(e.getMessage(), e);
			}
		}
		return -1;
	}

	public ConversationInfo getConversationInfo(long conversationID, boolean formatParticipants) {
		ConversationInfo info = new ConversationInfo();

		MonitoringPlugin plugin = (MonitoringPlugin) XMPPServer.getInstance().getPluginManager()
				.getPlugin("monitoring");

		ConversationManager conversationmanager = (ConversationManager) plugin.getModule(ConversationManager.class);
		try {
			Conversation conversation = conversationmanager.getConversation(conversationID);
			info = toConversationInfo(conversation, formatParticipants);
		} catch (NotFoundException e) {
			Log.error(e.getMessage(), e);
		}
		return info;
	}

	public Map<String, ConversationInfo> getConversations(boolean formatParticipants) {
		Map<String, ConversationInfo> cons = new HashMap<String, ConversationInfo>();
		MonitoringPlugin plugin = (MonitoringPlugin) XMPPServer.getInstance().getPluginManager()
				.getPlugin("monitoring");

		ConversationManager conversationManager = (ConversationManager) plugin.getModule(ConversationManager.class);

		Collection<Conversation> conversations = conversationManager.getConversations();
		List<Conversation> lConversations = Arrays
				.asList(conversations.toArray(new Conversation[conversations.size()]));
		for (Iterator<Conversation> i = lConversations.iterator(); i.hasNext();) {
			Conversation con = (Conversation) i.next();
			ConversationInfo info = toConversationInfo(con, formatParticipants);
			cons.put(Long.toString(con.getConversationID()), info);
		}
		return cons;
	}

	public ByteArrayOutputStream getConversationPDF(Conversation conversation) {
		Font red = FontFactory.getFont("Helvetica", 12.0F, 1, new Color(255, 0, 0));

		Font blue = FontFactory.getFont("Helvetica", 12.0F, 2, new Color(0, 0, 255));

		Font black = FontFactory.getFont("Helvetica", 12.0F, 1, Color.BLACK);

		Map<String, Font> colorMap = new HashMap<String, Font>();
		int count;
		if (conversation != null) {
			Collection<JID> set = conversation.getParticipants();
			count = 0;
			for (JID jid : set) {
				if (conversation.getRoom() == null) {
					if (count == 0) {
						colorMap.put(jid.toString(), blue);
					} else {
						colorMap.put(jid.toString(), red);
					}
					count++;
				} else {
					colorMap.put(jid.toString(), black);
				}
			}
		}
		return buildPDFContent(conversation, colorMap);
	}

	private ByteArrayOutputStream buildPDFContent(Conversation conversation, Map<String, Font> colorMap) {
		Font roomEvent = FontFactory.getFont("Helvetica", 12.0F, 2, new Color(255, 0, 255));
		try {
			Document document = new Document(PageSize.A4, 50.0F, 50.0F, 50.0F, 50.0F);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfWriter writer = PdfWriter.getInstance(document, baos);
			writer.setPageEvent(new PDFEventListener());
			document.open();

			Paragraph p = new Paragraph(LocaleUtils.getLocalizedString("archive.search.pdf.title", "monitoring"),
					FontFactory.getFont("Helvetica", 18.0F, 1));

			document.add(p);
			document.add(Chunk.NEWLINE);

			ConversationInfo coninfo = new ConversationUtils().getConversationInfo(conversation.getConversationID(),
					false);
			String participantsDetail;
			if (coninfo.getAllParticipants() == null) {
				participantsDetail = coninfo.getParticipant1() + ", " + coninfo.getParticipant2();
			} else {
				participantsDetail = String.valueOf(coninfo.getAllParticipants().length);
			}
			Paragraph chapterTitle = new Paragraph(
					LocaleUtils.getLocalizedString("archive.search.pdf.participants", "monitoring") + " "
							+ participantsDetail,
					FontFactory.getFont("Helvetica", 12.0F, 1));

			document.add(chapterTitle);

			Paragraph startDate = new Paragraph(
					LocaleUtils.getLocalizedString("archive.search.pdf.startdate", "monitoring") + " "
							+ coninfo.getDate(),
					FontFactory.getFont("Helvetica", 12.0F, 1));

			document.add(startDate);

			Paragraph duration = new Paragraph(
					LocaleUtils.getLocalizedString("archive.search.pdf.duration", "monitoring") + " "
							+ coninfo.getDuration(),
					FontFactory.getFont("Helvetica", 12.0F, 1));

			document.add(duration);

			Paragraph messageCount = new Paragraph(
					LocaleUtils.getLocalizedString("archive.search.pdf.messagecount", "monitoring") + " "
							+ conversation.getMessageCount(),
					FontFactory.getFont("Helvetica", 12.0F, 1));

			document.add(messageCount);
			document.add(Chunk.NEWLINE);
			for (ArchivedMessage message : conversation.getMessages()) {
				String time = JiveGlobals.formatTime(message.getSentDate());
				String from = message.getFromJID().getNode();
				if (conversation.getRoom() != null) {
					from = message.getToJID().getResource();
				}
				String body = message.getBody();
				Paragraph messageParagraph;
				if (!message.isRoomEvent()) {
					String prefix = "[" + time + "] " + from + ":  ";
					Font font = (Font) colorMap.get(message.getFromJID().toString());
					if (font == null) {
						font = (Font) colorMap.get(message.getFromJID().toBareJID());
					}
					if (font == null) {
						font = FontFactory.getFont("Helvetica", 12.0F, 1, Color.BLACK);
					}
					messageParagraph = new Paragraph(new Chunk(prefix, font));
				} else {
					String prefix = "[" + time + "] ";
					messageParagraph = new Paragraph(new Chunk(prefix, roomEvent));
				}
				messageParagraph.add(body);
				messageParagraph.add(" ");
				document.add(messageParagraph);
			}
			document.close();
			return baos;
		} catch (DocumentException e) {
			Log.error("error creating PDF document: " + e.getMessage(), e);
		}
		return null;
	}

	private ConversationInfo toConversationInfo(Conversation conversation, boolean formatParticipants) {
		ConversationInfo info = new ConversationInfo();

		Collection<JID> col = conversation.getParticipants();
		if (conversation.getRoom() == null) {
			JID user1 = (JID) col.toArray()[0];
			info.setParticipant1(formatJID(formatParticipants, user1));
			JID user2 = (JID) col.toArray()[1];
			info.setParticipant2(formatJID(formatParticipants, user2));
		} else {
			info.setConversationID(conversation.getConversationID());
			JID[] occupants = (JID[]) col.toArray(new JID[col.size()]);
			String[] jids = new String[col.size()];
			for (int i = 0; i < occupants.length; i++) {
				jids[i] = formatJID(formatParticipants, occupants[i]);
			}
			info.setAllParticipants(jids);
		}
		Map<String, String> cssLabels = new HashMap<String, String>();
		int count = 0;
		for (JID jid : col) {
			if (!cssLabels.containsKey(jid.toString())) {
				if (conversation.getRoom() == null) {
					if (count % 2 == 0) {
						cssLabels.put(jid.toBareJID(), "conversation-label2");
					} else {
						cssLabels.put(jid.toBareJID(), "conversation-label1");
					}
					count++;
				} else {
					cssLabels.put(jid.toString(), "conversation-label4");
				}
			}
		}
		info.setDate(JiveGlobals.formatDateTime(conversation.getStartDate()));
		info.setLastActivity(JiveGlobals.formatTime(conversation.getLastActivity()));

		StringBuilder builder = new StringBuilder();
		builder.append("<table width=100%>");
		for (ArchivedMessage message : conversation.getMessages()) {
			String time = JiveGlobals.formatTime(message.getSentDate());
			String from = message.getFromJID().getNode();
			if (conversation.getRoom() != null) {
				from = message.getToJID().getResource();
			}
			from = StringUtils.escapeHTMLTags(from);
			String cssLabel = (String) cssLabels.get(message.getFromJID().toBareJID());
			String body = StringUtils.escapeHTMLTags(message.getBody());
			builder.append("<tr valign=top>");
			if (!message.isRoomEvent()) {
				builder.append("<td width=1% nowrap class=" + cssLabel + ">").append("[").append(time).append("]")
						.append("</td>");

				builder.append("<td width=1% class=" + cssLabel + ">").append(from).append(": ").append("</td>");

				builder.append("<td class=conversation-body>").append(body).append("</td");
			} else {
				builder.append("<td width=1% nowrap class=conversation-label3>").append("[").append(time).append("]")
						.append("</td>");

				builder.append("<td colspan=2 class=conversation-label3><i>").append(body).append("</i></td");
			}
			builder.append("</tr>");
		}
		if (conversation.getMessages().size() == 0) {
			builder.append("<span class=small-description>"
					+ LocaleUtils.getLocalizedString("archive.search.results.archive_disabled", "monitoring") + "</a>");
		}
		info.setBody(builder.toString());

		info.setMessageCount(conversation.getMessageCount());

		long duration = conversation.getLastActivity().getTime() - conversation.getStartDate().getTime();

		info.setDuration(duration);

		return info;
	}

	private String formatJID(boolean html, JID jid) {
		String formattedJID;
		if (html) {
			UserManager userManager = UserManager.getInstance();
			if ((XMPPServer.getInstance().isLocal(jid)) && (userManager.isRegisteredUser(jid.getNode()))) {
				formattedJID = "<a href='/user-properties.jsp?username=" + jid.getNode() + "'>" + jid.toBareJID()
						+ "</a>";
			} else {
				formattedJID = jid.toBareJID();
			}
		} else {
			formattedJID = jid.toBareJID();
		}
		return formattedJID;
	}

	class PDFEventListener extends PdfPageEventHelper {
		PDFEventListener() {
		}

		public void onEndPage(PdfWriter writer, Document document) {
			PdfContentByte cb = writer.getDirectContent();
			try {
				cb.setColorStroke(new Color(156, 156, 156));
				cb.setLineWidth(2.0F);
				cb.moveTo(document.leftMargin(), document.bottomMargin() - 5.0F);
				cb.lineTo(document.getPageSize().width() - document.rightMargin(), document.bottomMargin() - 5.0F);

				cb.stroke();

				ClassLoader classLoader = ConversationUtils.class.getClassLoader();
				Enumeration<URL> providerEnum = classLoader.getResources("images/pdf_generatedbyof.gif");
				while (providerEnum.hasMoreElements()) {
					Image gif = Image.getInstance((URL) providerEnum.nextElement());
					cb.addImage(gif, 221.0F, 0.0F, 0.0F, 28.0F, (int) document.leftMargin(),
							(int) document.bottomMargin() - 35);
				}
			} catch (Exception e) {
				ConversationUtils.Log.error("error drawing PDF footer: " + e.getMessage());
			}
			cb.saveState();
		}
	}
}
