package com.mrcrayfish.device.proxy;

import com.mrcrayfish.device.DeviceConfig;
import com.mrcrayfish.device.MrCrayfishDeviceMod;
import com.mrcrayfish.device.Reference;
import com.mrcrayfish.device.api.ApplicationManager;
import com.mrcrayfish.device.api.app.Application;
import com.mrcrayfish.device.api.print.IPrint;
import com.mrcrayfish.device.api.print.PrintingManager;
import com.mrcrayfish.device.core.Laptop;
import com.mrcrayfish.device.core.client.ClientNotification;
import com.mrcrayfish.device.object.AppInfo;
import com.mrcrayfish.device.programs.system.SystemApplication;
import com.mrcrayfish.device.tileentity.*;
import com.mrcrayfish.device.tileentity.render.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy implements ISelectiveResourceReloadListener
{
    private static final Logger LOGGER = LogManager.getLogger("CDM-Client");

    @Override
    public void preInit()
    {
        super.preInit();
        ((IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(this);
    }

    @Override
    public void init()
    {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLaptop.class, new LaptopRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPrinter.class, new PrinterRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPaper.class, new PaperRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRouter.class, new RouterRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityOfficeChair.class, new OfficeChairRenderer());

        if(MrCrayfishDeviceMod.DEVELOPER_MODE)
        {
            Laptop.addWallpaper(new ResourceLocation("cdm:textures/gui/developer_wallpaper.png"));
        }
        else
        {
            Laptop.addWallpaper(new ResourceLocation("cdm:textures/gui/laptop_wallpaper_1.png"));
            Laptop.addWallpaper(new ResourceLocation("cdm:textures/gui/laptop_wallpaper_2.png"));
            Laptop.addWallpaper(new ResourceLocation("cdm:textures/gui/laptop_wallpaper_3.png"));
            Laptop.addWallpaper(new ResourceLocation("cdm:textures/gui/laptop_wallpaper_4.png"));
            Laptop.addWallpaper(new ResourceLocation("cdm:textures/gui/laptop_wallpaper_5.png"));
            Laptop.addWallpaper(new ResourceLocation("cdm:textures/gui/laptop_wallpaper_6.png"));
            Laptop.addWallpaper(new ResourceLocation("cdm:textures/gui/laptop_wallpaper_7.png"));
        }
    }

    @Override
    public void postInit()
    {
        generateIconAtlas();
    }

    /**
     * Had to recreate this, kept crashing harshly.
     */
    private void generateIconAtlas()
    {
        Minecraft minecraft = Minecraft.getMinecraft();
        final int maxIconSize = 14;
        int index = 0;

        BufferedImage atlas = new BufferedImage(maxIconSize * 16, maxIconSize * 16, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = atlas.createGraphics();

        try(IResource resource = minecraft.getResourceManager().getResource(new ResourceLocation(Reference.MOD_ID, "textures/app/icon/missing.png")))
        {
            BufferedImage icon = TextureUtil.readBufferedImage(resource.getInputStream());
            graphics.drawImage(icon, 0, 0, maxIconSize, maxIconSize, null);
        }
        catch (IOException ex)
        {
            LOGGER.error("Unable to fetch missing base icon texture", ex);
        }
        index++;

        for(AppInfo info : ApplicationManager.getAllApplications())
        {
            if(info.getIcon() == null)
            {
                continue;
            }

            ResourceLocation path = new ResourceLocation(info.getIcon());
            try(IResource resource = minecraft.getResourceManager().getResource(path))
            {
                BufferedImage icon = TextureUtil.readBufferedImage(resource.getInputStream());
                if(icon != null)
                {
                    if(icon.getWidth() != maxIconSize || icon.getHeight() != maxIconSize)
                    {
                        LOGGER.error("Incorrect icon size for " + info.getId() + " (Must be 14 by 14 pixels)");
                        continue;
                    }

                    int iconU = (index % 16) * maxIconSize;
                    int iconV = (index / 16) * maxIconSize;
                    graphics.drawImage(icon, iconU, iconV, maxIconSize, maxIconSize, null);
                    updateIcon(info, iconU, iconV);
                    index++;
                }
                else
                {
                    LOGGER.error("Icon for application '{}' could not be found at '{}'", info.getId(), path);
                }
            }
            catch(IOException ex)
            {
                LOGGER.error("Unable to load icon for application {} as it cannot be found", info.getId(), ex);
            }
        }

        graphics.dispose();
        minecraft.getTextureManager().loadTexture(Laptop.ICON_TEXTURES, new DynamicTexture(atlas));
    }

    private void updateIcon(AppInfo info, int iconU, int iconV)
    {
        ObfuscationReflectionHelper.setPrivateValue(AppInfo.class, info, iconU, "iconU");
        ObfuscationReflectionHelper.setPrivateValue(AppInfo.class, info, iconV, "iconV");
    }

    @Nullable
    @Override
    public Application registerApplication(ResourceLocation identifier, Class<? extends Application> clazz)
    {
        if("minecraft".equals(identifier.getNamespace()))
        {
            throw new IllegalArgumentException("Invalid identifier domain");
        }

        try
        {
            Application application = clazz.newInstance();
            java.util.List<Application> APPS = ObfuscationReflectionHelper.getPrivateValue(Laptop.class, null, "APPLICATIONS");
            APPS.add(application);

            Field field = Application.class.getDeclaredField("info");
            field.setAccessible(true);

            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(application, generateAppInfo(identifier, clazz));

            return application;
        }
        catch(InstantiationException | IllegalAccessException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @NotNull
    private AppInfo generateAppInfo(ResourceLocation identifier, Class<? extends Application> clazz)
    {
        AppInfo info = new AppInfo(identifier, SystemApplication.class.isAssignableFrom(clazz));
        info.reload();
        return info;
    }

    @Override
    public boolean registerPrint(ResourceLocation identifier, Class<? extends IPrint> classPrint)
    {
        try
        {
            Constructor<? extends IPrint> constructor = classPrint.getConstructor();
            IPrint print = constructor.newInstance();
            Class<? extends IPrint.Renderer> classRenderer = print.getRenderer();
            try
            {
                IPrint.Renderer renderer = classRenderer.newInstance();
                Map<String, IPrint.Renderer> idToRenderer = ObfuscationReflectionHelper.getPrivateValue(PrintingManager.class, null, "registeredRenders");
                if(idToRenderer == null)
                {
                    idToRenderer = new HashMap<>();
                    ObfuscationReflectionHelper.setPrivateValue(PrintingManager.class, null, idToRenderer, "registeredRenders");
                }
                idToRenderer.put(identifier.toString(), renderer);
            }
            catch(InstantiationException e)
            {
                MrCrayfishDeviceMod.getLogger().error("The print renderer '" + classRenderer.getName() + "' is missing an empty constructor and could not be registered!");
                return false;
            }
            return true;
        }
        catch(Exception e)
        {
            MrCrayfishDeviceMod.getLogger().error("The print '" + classPrint.getName() + "' is missing an empty constructor and could not be registered!");
        }
        return false;
    }

    @Override
    public void onResourceManagerReload(@NotNull IResourceManager resourceManager, @NotNull Predicate<IResourceType> resourcePredicate)
    {
        if(resourcePredicate.test(VanillaResourceType.TEXTURES))
        {
            if(ApplicationManager.getAllApplications().size() > 0)
            {
                ApplicationManager.getAllApplications().forEach(AppInfo::reload);
                generateIconAtlas();
            }
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        allowedApps = null;
        DeviceConfig.restore();
    }

    @Override
    public void showNotification(NBTTagCompound tag)
    {
        ClientNotification notification = ClientNotification.loadFromTag(tag);
        notification.push();
    }
}
