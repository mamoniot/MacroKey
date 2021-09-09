package com.mattsmeets.macrokey;

import com.mattsmeets.macrokey.model.*;
import com.mattsmeets.macrokey.config.ModConfig;
import com.mattsmeets.macrokey.MacroKey;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class BindingsRepository {
	private File bindingFile;

	private UUID activeLayer;
	private HashMap<Integer, ArrayList<Macro>> macros;
	private ArrayList<Layer> layers;

	/**
	* Initialisation of the repository
	* Note:
	* The repository will automatically
	* sync when initialized
	*
	*/
	public BindingsRepository(String path) {
		// set-up the bindings.json service & files
		bindingFile = new File(path + "/macrokey/" + ModConfig.bindingFile);

		File parentFolder = new File(bindingFile.getParent());

		try {
			if (!parentFolder.exists()) {
				parentFolder.mkdirs();
			}
		} catch(Exception e) {
			//TODO: error logging
		}

		loadConfiguration();
	}

	/**
	* Find all layers
	*
	* @param sync update from file before retrieving all layers
	* @return list of all layers
	*/
	public ArrayList<Layer> getLayers(boolean sync) {
		if (sync)
		// if specified to update memory with latest changes
		loadConfiguration();

		return this.layers;
	}
	public Layer getLayer(UUID ulid, boolean sync) {
		if (sync)
		// if specified to update memory with latest changes
			loadConfiguration();

		if(ulid == null) return null;

		for(int i = 0; i < layers.size(); i++) {
			Layer layer = layers.get(i);
			if(layer.ulid.equals(ulid)) {
				return layer;
			}
		}

		return null;
	}


	/**
	* Find layer by UUID
	*
	* @param ulid UUID
	* @param sync boolean update from file before retrieving
	* @return the layer found, may be null
	*/
	public Layer getActiveLayer(boolean sync) {
		if (sync)
		loadConfiguration();

		for(Layer layer : layers) {
			if(layer.ulid.equals(activeLayer)) {
				return layer;
			}
		}

		return null;
	}

	/**
	* Add Layder
	*
	* @param layer affected layer
	* @param sync  update file after adding layer
	*/
	public void addLayer(Layer layer, boolean sync) {
		this.layers.add(layer);

		if (sync) {
			// if specified to update configuration
			saveConfiguration();
		}
	}

	/**
	* Remove Layer by UUID
	*
	* @param ulid the unique layer identifier of the affected layer
	* @param sync update file after adding macro
	*/
	public void deleteLayer(UUID ulid, boolean sync) {
		for(int i = 0; i < layers.size(); i++) {
			Layer layer = layers.get(i);
			if(layer.ulid.equals(ulid)) {
				layers.remove(i);
				break;
			}
		}

		if (this.activeLayer.equals(ulid)) this.setActiveLayer(null, false);

		if (sync) {
			// if specified to update configuration
			saveConfiguration();
		}
	}


	public Layer setNextActiveLayer(boolean sync) {
		Layer ret = null;
		if(activeLayer == null) {
			if(layers.size() > 0) {
				ret = layers.get(0);
			}
		} else {
			for(int i = 0; i < layers.size() - 1; i++) {
				Layer layer = layers.get(i);
				if(layer.ulid.equals(activeLayer)) {
					ret = layers.get(i + 1);
					break;
				}
			}
		}
		activeLayer = ret == null ? null : ret.ulid;


		if (sync) {
			// if specified to update configuration
			saveConfiguration();
		}
		return ret;
	}

	/**
	* Find all active macro's
	*
	* @param sync update from file before retrieving all macros
	* @return list of all macros
	*/
	public HashMap<Integer, ArrayList<Macro>> getMacros(boolean sync) {
		if (sync)
		// if specified to update memory with latest changes
		loadConfiguration();

		return this.macros;
	}

	/**
	* Find active macro's by its keycode
	*
	* @param keyCode uses Keyboard keyCode
	* @param sync    update from file before retrieving all macros
	* @return list of active macro's with the given keyCode as trigger, or null if none exist
	*/
	public ArrayList<Macro> findMacroByKeycode(int keyCode, Layer layer, boolean sync) {
		if (sync)
		// if specified to update memory with latest changes
		loadConfiguration();

		ArrayList<Macro> ms = this.macros.get(keyCode);
		if(layer == null || ms == null) {
			return ms;
		} else {
			ArrayList<Macro> ret = new ArrayList(ms.size());
			for(Macro macro : ms) {
				if(layer.macros.contains(macro.umid)) {
					ret.add(macro);
				}
			}
			return ret;
		}
	}

	/**
	* Add Macro
	*
	* @param macro affected macro
	* @param sync  update file after adding macro
	*/
	public void addMacro(Macro macro, boolean sync) {
		ArrayList<Macro> ms = this.macros.get(macro.keyCode);
		if(ms == null) {
			ms = new ArrayList<Macro>();
			this.macros.put(macro.keyCode, ms);
		}
		ms.add(macro);
		if (sync) {
			// if specified to update configuration
			saveConfiguration();
		}
	}

	public void changeMacroKeyCode(Macro macro, int newKeyCode, boolean sync) {
		if(macro.keyCode == newKeyCode) return;

		ArrayList<Macro> ms = this.macros.get(macro.keyCode);
		ArrayList<Macro> ns = this.macros.get(newKeyCode);

		for(int i = 0; i < ms.size(); i++) {
			Macro m = ms.get(i);
			if(m == macro) {
				ms.remove(i);
				if(ms.size() == 0) {
					macros.remove(macro.keyCode);
				}
				if(ns == null) {
					ns = new ArrayList<Macro>();
					this.macros.put(newKeyCode, ns);
				}
				macro.keyCode = newKeyCode;
				ns.add(macro);
				break;
			}
		}

		if (sync) {
			// if specified to update configuration
			saveConfiguration();
		}
	}


	/**
	* Remove Macro by UUID
	*
	* @param umid    the unique macro identifier of the affected macro
	* @param sync    update file after adding macro
	*/
	public void deleteMacro(Macro macro, boolean sync) {
		ArrayList<Macro> ms = macros.get(macro.keyCode);

		for(int i = 0; i < ms.size(); i++) {
			Macro m = ms.get(i);
			if(m == macro) {
				ms.remove(i);
				if(ms.size() == 0) {
					macros.remove(macro.keyCode);
				}
				break;
			}
		}

		for (Layer layer : layers) {
			layer.macros.remove(macro.umid);
		}

		if (sync) {
			// if specified to update configuration
			saveConfiguration();
		}
	}

	/**
	* Set the active layer by ULID
	*
	* @param ulid unique layer id
	* @param sync update file after setting active layer
	*/
	public void setActiveLayer(UUID ulid, boolean sync) {
		this.activeLayer = ulid;

		if (sync) {
			// if specified to update configuration
			saveConfiguration();
		}
	}


	public static void writeString(ByteBuf buffer, String s) {
		byte[] bytes = s.getBytes(Charsets.UTF_8);
		buffer.writeInt(bytes.length);
		buffer.writeBytes(bytes);
		int word_padding = (4 - (bytes.length)%4)%4;
		for(int i = 0; i < word_padding; i++) {
			buffer.writeByte(0);
		}
	}

	public static String readString(ByteBuf buffer) {
		try {
			int length = buffer.readInt();
			byte[] bytes = new byte[length];
			buffer.readBytes(bytes);
			int word_padding = (4 - (length)%4)%4;
			for(int i = 0; i < word_padding; i++) {
				buffer.readByte();
			}
			return new String(bytes, Charsets.UTF_8);
		} catch (IndexOutOfBoundsException var2) {
			return null;
		}
	}

	public void saveConfiguration() {
		ByteBuf buffer = Unpooled.buffer();
		try {
			buffer.writeInt(3);//version
			writeString(buffer, activeLayer == null ? "" : activeLayer.toString());

			buffer.writeInt(macros.size());
			for (Map.Entry<Integer, ArrayList<Macro>> entry : macros.entrySet()) {
				int keycode = entry.getKey();
				ArrayList<Macro> ms = entry.getValue();
				buffer.writeInt(keycode);
				buffer.writeInt(ms.size());
				for(int i = 0; i < ms.size(); i++) {
					Macro macro = ms.get(i);
					writeString(buffer, macro.umid.toString());
					writeString(buffer, macro.command);
					buffer.writeInt(macro.flags);
				}
			}

			buffer.writeInt(layers.size());
			for (Layer layer : layers) {
				writeString(buffer, layer.ulid.toString());
				writeString(buffer, layer.displayName);
				int macros_size = layer.macros.size();
				buffer.writeInt(macros_size);
				for(UUID umid : layer.macros) {
					writeString(buffer, umid.toString());
				}
			}

			byte[] rawdata = buffer.array();
			FileOutputStream out = new FileOutputStream(bindingFile);
			out.write(rawdata);
		} catch(Exception e) {
			//TODO: file io error logging
			e.printStackTrace();
		} finally {
			buffer.release();
		}
	}

	public void setDefaultConfiguration() {
		activeLayer = null;
		macros = new HashMap<Integer, ArrayList<Macro>>();
		layers = new ArrayList<Layer>();
	}

	public void loadConfiguration() {
		ByteBuf buffer = Unpooled.buffer();
		try {
			if(bindingFile.length() < 4) {
				setDefaultConfiguration();
				return;
			}
			buffer.writeBytes(new FileInputStream(bindingFile), (int)bindingFile.length());

			int version = buffer.readInt();
			if(version != 3) {
				setDefaultConfiguration();
				return;
			}
			String str = readString(buffer);
			if(str == null) {
				setDefaultConfiguration();
				return;
			}
			if(str.equals("")) {
				activeLayer = null;
			} else {
				activeLayer = UUID.fromString(str);//throws if invalid
			}

			int s = buffer.readInt();
			macros = new HashMap<Integer, ArrayList<Macro>>(Math.max(16, s));
			for (int i = 0; i < s; i++) {
				int keycode = buffer.readInt();
				int macros_size = buffer.readInt();
				ArrayList<Macro> macro_list = new ArrayList<Macro>(macros_size);
				for(int j = 0; j < macros_size; j++) {
					Macro macro = new Macro();
					str = readString(buffer);
					if(str == null) {
						setDefaultConfiguration();
						return;
					}
					macro.umid = UUID.fromString(str);//throws if invalid
					macro.command = readString(buffer);
					if(macro.command == null) {
						setDefaultConfiguration();
						return;
					}
					macro.flags = buffer.readInt();
					macro.keyCode = keycode;
					macro_list.add(macro);
				}
				macros.put(keycode, macro_list);
			}

			s = buffer.readInt();
			layers = new ArrayList<Layer>(s);
			for (int i = 0; i < s; i++) {
				Layer layer = new Layer();
				str = readString(buffer);
				if(str == null) {
					setDefaultConfiguration();
					return;
				}
				layer.ulid = UUID.fromString(str);//throws if invalid
				layer.displayName = readString(buffer);
				if(layer.displayName == null) {
					setDefaultConfiguration();
					return;
				}
				int macros_size = buffer.readInt();
				for(int j = 0; j < macros_size; j++) {
					str = readString(buffer);
					if(str == null) {
						setDefaultConfiguration();
						return;
					}
					layer.macros.add(UUID.fromString(str));//throws if invalid
				}
				layers.add(layer);
			}

		} catch(Exception e) {
			e.printStackTrace();
			setDefaultConfiguration();
		} finally {
			buffer.release();
		}
	}

}
