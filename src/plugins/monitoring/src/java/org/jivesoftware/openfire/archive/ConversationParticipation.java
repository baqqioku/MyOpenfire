package org.jivesoftware.openfire.archive;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;
import org.jivesoftware.util.cache.ExternalizableUtil;

public class ConversationParticipation implements Externalizable {
	private Date joined = new Date();
	private Date left;
	private String nickname;

	public ConversationParticipation() {
	}

	public ConversationParticipation(Date joined) {
		this.joined = joined;
	}

	public ConversationParticipation(Date joined, String nickname) {
		this.joined = joined;
		this.nickname = nickname;
	}

	public void participationEnded(Date left) {
		this.left = left;
	}

	public Date getJoined() {
		return this.joined;
	}

	public Date getLeft() {
		return this.left;
	}

	public String getNickname() {
		return this.nickname;
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		ExternalizableUtil.getInstance().writeLong(out, this.joined.getTime());
		ExternalizableUtil.getInstance().writeBoolean(out, this.nickname != null);
		if (this.nickname != null) {
			ExternalizableUtil.getInstance().writeSafeUTF(out, this.nickname);
		}
		ExternalizableUtil.getInstance().writeBoolean(out, this.left != null);
		if (this.left != null) {
			ExternalizableUtil.getInstance().writeLong(out, this.left.getTime());
		}
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.joined = new Date(ExternalizableUtil.getInstance().readLong(in));
		if (ExternalizableUtil.getInstance().readBoolean(in)) {
			this.nickname = ExternalizableUtil.getInstance().readSafeUTF(in);
		}
		if (ExternalizableUtil.getInstance().readBoolean(in)) {
			this.left = new Date(ExternalizableUtil.getInstance().readLong(in));
		}
	}
}
