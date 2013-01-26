/*
 * This file is part of Technic Launcher.
 *
 * Copyright (c) 2013-2013, Technic <http://www.technicpack.net/>
 * Technic Launcher is licensed under the Spout License Version 1.
 *
 * Technic Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the Spout License Version 1.
 *
 * Technic Launcher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the Spout License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
 * including the MIT license.
 */
package org.spoutcraft.launcher;

import java.io.IOException;

import org.spoutcraft.launcher.api.Launcher;
import org.spoutcraft.launcher.api.Directories;
import org.spoutcraft.launcher.exceptions.RestfulAPIException;
import org.spoutcraft.launcher.launch.MinecraftLauncher;
import org.spoutcraft.launcher.technic.InstalledPack;
import org.spoutcraft.launcher.technic.Modpack;
import org.spoutcraft.launcher.util.DownloadListener;

public final class GameUpdater extends Directories {
	public static final String baseURL = "http://s3.amazonaws.com/MinecraftDownload/";
	public static final String latestLWJGLURL = "http://get.spout.org/lib/lwjgl/";
	public static final String spoutcraftMirrors = "http://get.spout.org/mirrors.yml";

	// Minecraft Updating Arguments
	private String user = "Player";
	private String downloadTicket = "1";
	private String minecraftPass = "";
	private String minecraftSession = "";

	private DownloadListener listener;
	private Modpack modpack;
	private InstalledPack installedPack;
	private long validationTime;
	private UpdateThread updateThread;

	public GameUpdater() {
	}

	public Modpack getBuild() {
		return modpack;
	}

	public void start(InstalledPack installedPack, UpdateThread updateThread) throws RestfulAPIException {
		this.installedPack = installedPack;
		this.modpack = installedPack.getModpack();
		this.updateThread = updateThread;
		updateThread.start();
	}

	public boolean isFinished() {
		return updateThread.isFinished();
	}

	public void setWaiting(boolean waiting) {
		updateThread.setWaiting(waiting);
	}

	public void onModpackBuildChange(InstalledPack installedPack) {
		Modpack prev = this.modpack;
		try {
			this.modpack = installedPack.getModpack();
			if (prev == null || !this.modpack.getBuild().equals(prev.getBuild())) {
				if (updateThread != null) {
					updateThread.setDownloadListener(null);
					updateThread.interrupt();
				}

				updateThread = new UpdateThread(installedPack, listener);
	
				MinecraftLauncher.resetClassLoader();
				start(installedPack, updateThread);
			}
		} catch (IOException e) {
			Launcher.getLoginFrame().handleException(e);
		}
	}

	public void setStartValidationTime(long validationTime) {
		this.validationTime = validationTime;
	}

	protected void validationFinished(boolean result) {
		long end = System.currentTimeMillis();
		System.out.println("------------------ Validation Finished  ------------------");
		System.out.println("Finished in " + (end - validationTime) + "ms");
		System.out.println("Result: " + result);
	}


	public void setMinecraftUser(String user) {
		this.user = user;
	}

	public void setDownloadTicket(String downloadTicket) {
		this.downloadTicket = downloadTicket;
	}

	public String getMinecraftUser() {
		return user;
	}

	public String getDownloadTicket() {
		return downloadTicket;
	}

	public String getMinecraftPass() {
		return minecraftPass;
	}

	public String getMinecraftSession() {
		return minecraftSession;
	}

	public void setMinecraftPass(String minecraftPass) {
		this.minecraftPass = minecraftPass;
	}

	public void setMinecraftSession(String minecraftSession) {
		this.minecraftSession = minecraftSession;
	}

	public DownloadListener getDownloadListener() {
		return listener;
	}

	public void setDownloadListener(DownloadListener listener) {
		this.listener = listener;
	}

	public void stateChanged(String message, float progress) {
		if (listener != null) {
			listener.stateChanged(message, progress);
		}
	}

	public void runGame() {
		Launcher.getGameLauncher().runGame(user, minecraftSession, downloadTicket, this.installedPack);
	}
}
