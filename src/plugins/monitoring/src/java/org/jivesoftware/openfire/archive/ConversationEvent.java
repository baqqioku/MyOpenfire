package org.jivesoftware.openfire.archive;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.xmpp.packet.JID;

public class ConversationEvent implements Externalizable {
	private Type type;
	private Date date;
	private String body;
	private JID sender;
	private JID receiver;
	private JID roomJID;
	private JID user;
	private String nickname;

	public void run(ConversationManager conversationManager) {
		if (Type.chatMessageReceived == this.type) {
			conversationManager.processMessage(this.sender, this.receiver, this.body, "", this.date, "");
		} else if (Type.roomDestroyed == this.type) {
			conversationManager.roomConversationEnded(this.roomJID, this.date);
		} else if (Type.occupantJoined == this.type) {
			conversationManager.joinedGroupConversation(this.roomJID, this.user, this.nickname, this.date);
		} else if (Type.occupantLeft == this.type) {
			conversationManager.leftGroupConversation(this.roomJID, this.user, this.date);

			MUCRoom mucRoom = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(this.roomJID)
					.getChatRoom(this.roomJID.getNode());
			if ((mucRoom != null) && (mucRoom.getOccupantsCount() == 0)) {
				conversationManager.roomConversationEnded(this.roomJID, this.date);
			}
		} else if (Type.nicknameChanged == this.type) {
			conversationManager.leftGroupConversation(this.roomJID, this.user, this.date);
			conversationManager.joinedGroupConversation(this.roomJID, this.user, this.nickname,
					new Date(this.date.getTime() + 1L));
		} else if (Type.roomMessageReceived == this.type) {
			conversationManager.processRoomMessage(this.roomJID, this.user, this.nickname, this.body, this.date, "");
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		ExternalizableUtil.getInstance().writeInt(out, this.type.ordinal());
		ExternalizableUtil.getInstance().writeLong(out, this.date.getTime());
		ExternalizableUtil.getInstance().writeBoolean(out, this.sender != null);
		if (this.sender != null) {
			ExternalizableUtil.getInstance().writeSerializable(out, this.sender);
		}
		ExternalizableUtil.getInstance().writeBoolean(out, this.receiver != null);
		if (this.receiver != null) {
			ExternalizableUtil.getInstance().writeSerializable(out, this.receiver);
		}
		ExternalizableUtil.getInstance().writeBoolean(out, this.body != null);
		if (this.body != null) {
			ExternalizableUtil.getInstance().writeSafeUTF(out, this.body);
		}
		ExternalizableUtil.getInstance().writeBoolean(out, this.roomJID != null);
		if (this.roomJID != null) {
			ExternalizableUtil.getInstance().writeSerializable(out, this.roomJID);
		}
		ExternalizableUtil.getInstance().writeBoolean(out, this.user != null);
		if (this.user != null) {
			ExternalizableUtil.getInstance().writeSerializable(out, this.user);
		}
		ExternalizableUtil.getInstance().writeBoolean(out, this.nickname != null);
		if (this.nickname != null) {
			ExternalizableUtil.getInstance().writeSafeUTF(out, this.nickname);
		}
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.type = Type.values()[ExternalizableUtil.getInstance().readInt(in)];
		this.date = new Date(ExternalizableUtil.getInstance().readLong(in));
		if (ExternalizableUtil.getInstance().readBoolean(in)) {
			this.sender = ((JID) ExternalizableUtil.getInstance().readSerializable(in));
		}
		if (ExternalizableUtil.getInstance().readBoolean(in)) {
			this.receiver = ((JID) ExternalizableUtil.getInstance().readSerializable(in));
		}
		if (ExternalizableUtil.getInstance().readBoolean(in)) {
			this.body = ExternalizableUtil.getInstance().readSafeUTF(in);
		}
		if (ExternalizableUtil.getInstance().readBoolean(in)) {
			this.roomJID = ((JID) ExternalizableUtil.getInstance().readSerializable(in));
		}
		if (ExternalizableUtil.getInstance().readBoolean(in)) {
			this.user = ((JID) ExternalizableUtil.getInstance().readSerializable(in));
		}
		if (ExternalizableUtil.getInstance().readBoolean(in)) {
			this.nickname = ExternalizableUtil.getInstance().readSafeUTF(in);
		}
	}

	public static ConversationEvent chatMessageReceived(JID sender, JID receiver, String body, Date date) {
		ConversationEvent event = new ConversationEvent();
		event.type = Type.chatMessageReceived;
		event.sender = sender;
		event.receiver = receiver;
		event.body = body;
		event.date = date;
		return event;
	}

	public static ConversationEvent roomDestroyed(JID roomJID, Date date) {
		ConversationEvent event = new ConversationEvent();
		event.type = Type.roomDestroyed;
		event.roomJID = roomJID;
		event.date = date;
		return event;
	}

	public static ConversationEvent occupantJoined(JID roomJID, JID user, String nickname, Date date) {
		ConversationEvent event = new ConversationEvent();
		event.type = Type.occupantJoined;
		event.roomJID = roomJID;
		event.user = user;
		event.nickname = nickname;
		event.date = date;
		return event;
	}

	public static ConversationEvent occupantLeft(JID roomJID, JID user, Date date) {
		ConversationEvent event = new ConversationEvent();
		event.type = Type.occupantLeft;
		event.roomJID = roomJID;
		event.user = user;
		event.date = date;
		return event;
	}

	public static ConversationEvent nicknameChanged(JID roomJID, JID user, String newNickname, Date date) {
		ConversationEvent event = new ConversationEvent();
		event.type = Type.nicknameChanged;
		event.roomJID = roomJID;
		event.user = user;
		event.nickname = newNickname;
		event.date = date;
		return event;
	}

	public static ConversationEvent roomMessageReceived(JID roomJID, JID user, String nickname, String body,
			Date date) {
		ConversationEvent event = new ConversationEvent();
		event.type = Type.roomMessageReceived;
		event.roomJID = roomJID;
		event.user = user;
		event.nickname = nickname;
		event.body = body;
		event.date = date;
		return event;
	}

	private static enum Type {
		roomDestroyed, occupantJoined, occupantLeft, nicknameChanged, roomMessageReceived, chatMessageReceived;

		private Type() {
		}
	}
}
