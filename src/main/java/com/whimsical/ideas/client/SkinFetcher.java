package com.whimsical.ideas.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.whimsical.ideas.WhimsicalIdeas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class SkinFetcher {

    private static final Map<String, ResourceLocation> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> LOADING = new ConcurrentHashMap<>();

    public static final ResourceLocation FALLBACK =
            new ResourceLocation(WhimsicalIdeas.MOD_ID, "textures/entity/plushie.png");

    private static File cacheDir() {
        File dir = new File(
                new File(Minecraft.getInstance().gameDirectory, "config/doll"),
                "cache"
        );
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    // ==================== 联网皮肤 ====================

    public static ResourceLocation getSkin(String name) {
        if (name == null || name.isEmpty()) return FALLBACK;

        ResourceLocation cached = CACHE.get(name);
        if (cached != null) return cached;
        if (LOADING.containsKey(name)) return FALLBACK;

        LOADING.put(name, true);
        CompletableFuture.runAsync(() -> {
            File cacheFile = new File(cacheDir(), safeName(name) + ".png");
            if (cacheFile.isFile()) {
                try (InputStream in = new FileInputStream(cacheFile)) {
                    NativeImage image = NativeImage.read(in);
                    registerTexture(name, image);
                    return;
                } catch (Exception ignored) {}
            }
            fetchAndRegister(name);
        });

        return FALLBACK;
    }

    public static boolean isReady(String name) {
        if (name == null || name.isEmpty()) return true;
        return CACHE.containsKey(name);
    }

    public static void fetchSkinBytes(String name, Consumer<byte[]> callback) {
        if (name == null || name.isEmpty()) {
            callback.accept(null);
            return;
        }

        CompletableFuture.runAsync(() -> {
            byte[] data = null;

            File cacheFile = new File(cacheDir(), safeName(name) + ".png");
            if (cacheFile.isFile()) {
                try (InputStream in = new FileInputStream(cacheFile)) {
                    data = in.readAllBytes();
                } catch (Exception ignored) {}
            }

            if (data == null) {
                int mode = com.whimsical.ideas.PlushieCraftingTableScreen.getSkinApiMode();
                if (mode == 0) {
                    try {
                        String uuid = queryUUID(name);
                        if (uuid != null) {
                            String skinUrl = querySkinUrl(uuid);
                            if (skinUrl != null) {
                                data = downloadBytes(skinUrl);
                            }
                        }
                    } catch (Exception ignored) {}
                } else {
                    data = downloadBytes("https://littleskin.cn/skin/" + name + ".png");
                }
            }

            if (data != null) {
                saveCacheFile(name, data);
            }

            final byte[] result = data;
            Minecraft.getInstance().execute(() -> callback.accept(result));
        });
    }

    // ==================== 本地图片 ====================

    public static ResourceLocation getLocalSkin(String fileName) {
        if (fileName == null || fileName.isEmpty()) return FALLBACK;

        String cacheKey = "local:" + fileName;
        ResourceLocation cached = CACHE.get(cacheKey);
        if (cached != null) return cached;
        if (LOADING.containsKey(cacheKey)) return FALLBACK;

        LOADING.put(cacheKey, true);
        CompletableFuture.runAsync(() -> {
            try {
                File file = new File(
                        new File(Minecraft.getInstance().gameDirectory, "config/doll"),
                        fileName
                );
                if (!file.isFile()) {
                    CACHE.put(cacheKey, FALLBACK);
                    LOADING.remove(cacheKey);
                    return;
                }
                NativeImage image;
                try (InputStream in = new FileInputStream(file)) {
                    image = NativeImage.read(in);
                }
                registerLocalTexture(cacheKey, fileName, image);
            } catch (Exception e) {
                WhimsicalIdeas.LOGGER.error("SkinFetcher: local load failed for {}", fileName, e);
                CACHE.put(cacheKey, FALLBACK);
                LOADING.remove(cacheKey);
            }
        });

        return FALLBACK;
    }

    public static boolean isLocalReady(String fileName) {
        if (fileName == null || fileName.isEmpty()) return true;
        return CACHE.containsKey("local:" + fileName);
    }

    public static void fetchLocalBytes(String fileName, Consumer<byte[]> callback) {
        if (fileName == null || fileName.isEmpty()) {
            callback.accept(null);
            return;
        }
        CompletableFuture.runAsync(() -> {
            byte[] data = null;
            try {
                File file = new File(
                        new File(Minecraft.getInstance().gameDirectory, "config/doll"),
                        fileName
                );
                if (file.isFile()) {
                    try (InputStream in = new FileInputStream(file)) {
                        data = in.readAllBytes();
                    }
                }
            } catch (Exception ignored) {}
            final byte[] result = data;
            Minecraft.getInstance().execute(() -> callback.accept(result));
        });
    }

    public static List<String> listLocalSkins() {
        File dollDir = new File(Minecraft.getInstance().gameDirectory, "config/doll");
        if (!dollDir.isDirectory()) return Collections.emptyList();
        File[] files = dollDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (File f : files) result.add(f.getName());
        Collections.sort(result);
        return result;
    }

    // ==================== NBT 字节 ====================

    public static ResourceLocation registerFromBytes(byte[] data, String key) {
        if (data == null || data.length == 0) return FALLBACK;

        ResourceLocation cached = CACHE.get(key);
        if (cached != null) return cached;

        try {
            NativeImage image = NativeImage.read(new ByteArrayInputStream(data));
            registerTexture(key, image);
        } catch (Exception e) {
            WhimsicalIdeas.LOGGER.error("SkinFetcher: NBT read failed for {}", key, e);
            CACHE.put(key, FALLBACK);
        }

        return FALLBACK;
    }

    // ==================== 内部 ====================

    private static void fetchAndRegister(String name) {
        try {
            int mode = com.whimsical.ideas.PlushieCraftingTableScreen.getSkinApiMode();

            if (mode == 0) {
                String uuid = queryUUID(name);
                if (uuid != null) {
                    String skinUrl = querySkinUrl(uuid);
                    if (skinUrl != null) {
                        byte[] data = downloadBytes(skinUrl);
                        if (data != null) {
                            saveCacheFile(name, data);
                            registerTexture(name, NativeImage.read(new ByteArrayInputStream(data)));
                            return;
                        }
                    }
                }
            } else {
                byte[] data = downloadBytes("https://littleskin.cn/skin/" + name + ".png");
                if (data != null) {
                    saveCacheFile(name, data);
                    registerTexture(name, NativeImage.read(new ByteArrayInputStream(data)));
                    return;
                }
            }

            CACHE.put(name, FALLBACK);
            LOADING.remove(name);
        } catch (Exception e) {
            WhimsicalIdeas.LOGGER.error("SkinFetcher: fetch failed for {}", name, e);
            CACHE.put(name, FALLBACK);
            LOADING.remove(name);
        }
    }

    private static void registerTexture(String key, NativeImage image) {
        Minecraft.getInstance().execute(() -> {
            try {
                DynamicTexture texture = new DynamicTexture(image);
                ResourceLocation loc = new ResourceLocation(
                        WhimsicalIdeas.MOD_ID,
                        "skins/" + safeName(key)
                );
                Minecraft.getInstance().getTextureManager().register(loc, texture);
                CACHE.put(key, loc);
            } catch (Exception e) {
                WhimsicalIdeas.LOGGER.error("SkinFetcher: register failed for {}", key, e);
                CACHE.put(key, FALLBACK);
            } finally {
                LOADING.remove(key);
            }
        });
    }

    private static void registerLocalTexture(String cacheKey, String fileName, NativeImage image) {
        Minecraft.getInstance().execute(() -> {
            try {
                DynamicTexture texture = new DynamicTexture(image);
                ResourceLocation loc = new ResourceLocation(
                        WhimsicalIdeas.MOD_ID,
                        "local/" + safeName(fileName)
                );
                Minecraft.getInstance().getTextureManager().register(loc, texture);
                CACHE.put(cacheKey, loc);
            } catch (Exception e) {
                WhimsicalIdeas.LOGGER.error("SkinFetcher: local register failed for {}", fileName, e);
                CACHE.put(cacheKey, FALLBACK);
            } finally {
                LOADING.remove(cacheKey);
            }
        });
    }

    private static void saveCacheFile(String name, byte[] data) {
        try {
            File out = new File(cacheDir(), safeName(name) + ".png");
            try (FileOutputStream fos = new FileOutputStream(out)) {
                fos.write(data);
            }
        } catch (Exception ignored) {}
    }

    private static String safeName(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9_.-]", "_");
    }

    private static String queryUUID(String name) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            if (conn.getResponseCode() != 200) return null;
            try (InputStream in = conn.getInputStream()) {
                String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                return obj.get("id").getAsString();
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static String querySkinUrl(String uuid) {
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            if (conn.getResponseCode() != 200) return null;
            try (InputStream in = conn.getInputStream()) {
                String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                var properties = obj.getAsJsonArray("properties");
                for (var prop : properties) {
                    JsonObject p = prop.getAsJsonObject();
                    if ("textures".equals(p.get("name").getAsString())) {
                        String decoded = new String(
                                Base64.getDecoder().decode(p.get("value").getAsString()),
                                StandardCharsets.UTF_8
                        );
                        JsonObject textures = JsonParser.parseString(decoded)
                                .getAsJsonObject()
                                .getAsJsonObject("textures");
                        if (textures.has("SKIN")) {
                            return textures.getAsJsonObject("SKIN").get("url").getAsString();
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] downloadBytes(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "WhimsicalIdeas/1.0");
            if (conn.getResponseCode() != 200) return null;
            try (InputStream in = conn.getInputStream()) {
                return in.readAllBytes();
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static void clearCache() {
        CACHE.clear();
        LOADING.clear();
    }
}