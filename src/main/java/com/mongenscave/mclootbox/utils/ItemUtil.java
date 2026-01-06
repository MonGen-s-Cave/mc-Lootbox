package com.mongenscave.mclootbox.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

@SuppressWarnings("deprecation")
@UtilityClass
public class ItemUtil {

    public String serialize(ItemStack item) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

        dataOutput.writeObject(item);
        dataOutput.close();

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    public ItemStack deserialize(String data) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));

        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
        ItemStack item = (ItemStack) dataInput.readObject();
        dataInput.close();

        return item;
    }
}