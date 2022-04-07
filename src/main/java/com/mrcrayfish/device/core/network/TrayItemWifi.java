package com.mrcrayfish.device.core.network;

import com.mrcrayfish.device.DeviceConfig;
import com.mrcrayfish.device.api.app.Icons;
import com.mrcrayfish.device.api.app.Layout;
import com.mrcrayfish.device.api.app.component.ItemList;
import com.mrcrayfish.device.api.app.renderer.ListItemRenderer;
import com.mrcrayfish.device.api.task.TaskManager;
import com.mrcrayfish.device.api.utils.RenderUtil;
import com.mrcrayfish.device.core.Device;
import com.mrcrayfish.device.core.Laptop;
import com.mrcrayfish.device.core.network.task.TaskConnect;
import com.mrcrayfish.device.core.network.task.TaskPing;
import com.mrcrayfish.device.object.TrayItem;
import com.mrcrayfish.device.tileentity.TileEntityDevice;
import com.mrcrayfish.device.tileentity.TileEntityRouter;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.awt.*;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class TrayItemWifi extends TrayItem
{
    private int pingTimer;

    public TrayItemWifi()
    {
        super(Icons.WIFI_NONE);
    }

    @Override
    public void init()
    {
        this.setClickListener((mouseX, mouseY, mouseButton) ->
        {
            if(Laptop.getSystem().hasContext())
            {
                Laptop.getSystem().closeContext();
            }
            else
            {
                Laptop.getSystem().openContext(createWifiMenu(this), mouseX - 100, mouseY - 100);
            }
        });

        runPingTask();
    }

    @Override
    public void tick()
    {
        if(++pingTimer >= DeviceConfig.getPingRate())
        {
            runPingTask();
            pingTimer = 0;
        }
    }

    private void runPingTask()
    {
        TaskPing task = new TaskPing(Laptop.getPos());
        task.setCallback((tagCompound, success) ->
        {
            if(success)
            {
                int strength = tagCompound.getInteger("strength");
                switch(strength)
                {
                    case 2:
                        setIcon(Icons.WIFI_LOW);
                        break;
                    case 1:
                        setIcon(Icons.WIFI_MED);
                        break;
                    case 0:
                        setIcon(Icons.WIFI_HIGH);
                        break;
                    default:
                        setIcon(Icons.WIFI_NONE);
                        break;
                }
            }
            else
            {
                setIcon(Icons.WIFI_NONE);
            }
        });
        TaskManager.sendTask(task);
    }

    private static Layout createWifiMenu(TrayItem item)
    {
        Layout layout = new Layout.Context(100, 100);
        layout.setBackground((gui, mc, x, y, width, height, mouseX, mouseY, windowActive) ->
                Gui.drawRect(x, y, x + width, y + height, new Color(0.65F, 0.65F, 0.65F, 0.9F).getRGB()));

        ItemList<Device> itemListRouters = new ItemList<>(5, 5, 90, 4);
        itemListRouters.setItems(getRouters());
        itemListRouters.setListItemRenderer(new ListItemRenderer<Device>(16)
        {
            @Override
            public void render(Device device, Gui gui, Minecraft mc, int x, int y, int width, int height, boolean selected)
            {
                Gui.drawRect(x, y, x + width, y + height, selected ? Color.DARK_GRAY.getRGB() : Color.GRAY.getRGB());
                RenderUtil.drawStringClipped(device.getName(), x + 16, y + 4, 70, Color.WHITE.getRGB(), false);

                if(device.getPos() == null)
                    return;

                BlockPos laptopPos = Laptop.getPos();
                double distance = Math.sqrt(device.getPos().distanceSqToCenter(laptopPos.getX() + 0.5, laptopPos.getY() + 0.5, laptopPos.getZ() + 0.5));
                if(distance > 20)
                {
                    Icons.WIFI_LOW.draw(mc, x + 3, y + 3);
                }
                else if(distance > 10)
                {
                    Icons.WIFI_MED.draw(mc, x + 3, y + 3);
                }
                else
                {
                    Icons.WIFI_HIGH.draw(mc, x + 3, y + 3);
                }
            }
        });
        itemListRouters.sortBy((o1, o2) -> {
            BlockPos laptopPos = Laptop.getPos();
            double distance1 = Math.sqrt(o1.getPos().distanceSqToCenter(laptopPos.getX() + 0.5, laptopPos.getY() + 0.5, laptopPos.getZ() + 0.5));
            double distance2 = Math.sqrt(o2.getPos().distanceSqToCenter(laptopPos.getX() + 0.5, laptopPos.getY() + 0.5, laptopPos.getZ() + 0.5));
            return Double.compare(distance1, distance2);
        });
        layout.addComponent(itemListRouters);

        com.mrcrayfish.device.api.app.component.Button buttonConnect = new com.mrcrayfish.device.api.app.component.Button(79, 79, Icons.CHECK);
        buttonConnect.setClickListener((mouseX, mouseY, mouseButton) ->
        {
            if(mouseButton == 0)
            {
                if(itemListRouters.getSelectedItem() != null)
                {
                    TaskConnect connect = new TaskConnect(Laptop.getPos(), itemListRouters.getSelectedItem().getPos());
                    connect.setCallback((tagCompound, success) ->
                    {
                        if(success)
                        {
                            item.setIcon(Icons.WIFI_HIGH);
                            Laptop.getSystem().closeContext();
                        }
                    });
                    TaskManager.sendTask(connect);
                }
            }
        });
        layout.addComponent(buttonConnect);

        return layout;
    }

    private static List<Device> getRouters()
    {
        List<Device> routers = new ObjectArrayList<>();

        World world = Minecraft.getMinecraft().world;
        BlockPos laptopPos = Laptop.getPos();
        int range = DeviceConfig.getSignalRange();

        for(int y = -range; y < range + 1; y++)
        {
            for(int z = -range; z < range + 1; z++)
            {
                for(int x = -range; x < range + 1; x++)
                {
                    BlockPos pos = new BlockPos(laptopPos.getX() + x, laptopPos.getY() + y, laptopPos.getZ() + z);
                    TileEntity tileEntity = world.getTileEntity(pos);
                    if(tileEntity instanceof TileEntityRouter)
                    {
                        routers.add(new Device((TileEntityDevice) tileEntity));
                    }
                }
            }
        }
        return routers;
    }
}
