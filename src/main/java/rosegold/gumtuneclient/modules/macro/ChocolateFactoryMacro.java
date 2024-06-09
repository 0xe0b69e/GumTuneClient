package rosegold.gumtuneclient.modules.macro;

import cc.polyfrost.oneconfig.utils.Multithreading;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import rosegold.gumtuneclient.GumTuneClient;
import rosegold.gumtuneclient.config.GumTuneClientConfig;
import rosegold.gumtuneclient.events.ScreenClosedEvent;
import rosegold.gumtuneclient.utils.GuiUtils;
import rosegold.gumtuneclient.utils.InventoryUtils;
import rosegold.gumtuneclient.utils.ModUtils;
import rosegold.gumtuneclient.utils.StringUtils;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static rosegold.gumtuneclient.GumTuneClient.mc;

public class ChocolateFactoryMacro
{
  private boolean inChocolateFactory;
  private final ArrayList< ItemStack > currentInventory = new ArrayList<>( );
  private boolean clicked = true;
  
  private int chocolatePurse;
  private int bestRabbitCost = 0;
  private int bestRabbitSlot = 0;
  
  @SubscribeEvent
  public final void onGuiOpen ( @NotNull GuiOpenEvent event )
  {
    if ( !GumTuneClientConfig.chocolateFactoryMacro ) return;
    inChocolateFactory = GuiUtils.getInventoryName( event.gui ).equals( "Chocolate Factory" );
    currentInventory.clear( );
  }
  
  @SubscribeEvent
  public void onGuiClose ( ScreenClosedEvent event )
  {
    if ( !GumTuneClientConfig.chocolateFactoryMacro ) return;
    inChocolateFactory = false;
    currentInventory.clear( );
  }
  
  @SubscribeEvent
  public void onBackgroundDraw ( GuiScreenEvent.BackgroundDrawnEvent event )
  {
    if ( !inChocolateFactory ) return;
    if ( !clicked ) return;
    if ( mc.thePlayer.openContainer.inventorySlots.size( ) != currentInventory.size( ) )
    {
      for ( Slot slot : mc.thePlayer.openContainer.inventorySlots )
      {
        currentInventory.add( slot.getStack( ) );
      }
    }
    
    for ( int i = 0; i < currentInventory.size( ); i++ )
    {
      ItemStack stack = currentInventory.get( i );
      
      if ( stack != null && stack.getItem( ) == Items.skull )
      {
        Pattern pattern = Pattern.compile( "\\d+" );
        
        String displayName = StringUtils.removeFormatting( stack.getDisplayName( ) );
        
        if ( !displayName.contains( "Chocolate" ) && !displayName.contains( "Rabbit" ) )
          continue;
        
        if ( displayName.contains( "Chocolate" ) && !displayName.contains( "Rabbit" ) ) // The rabbit head containing chocolate factory info
        {
          String raw = displayName.replace( ",", "" );
          Matcher matcher = pattern.matcher( raw );
          if ( matcher.find( ) )
          {
            int purse = Integer.parseInt( matcher.group( ) );
            if ( purse == chocolatePurse )
            {
              currentInventory.clear( );
              return;
            }
            chocolatePurse = purse;
          }
        }
        
        if ( !displayName.contains( "Chocolate" ) && displayName.contains( "Rabbit" ) ) // Employees
        {
          String amountLore = StringUtils.removeFormatting( Objects.requireNonNull( InventoryUtils.getItemLore( stack, 4 ) ) ).replace( ",", "" );
          String costLore = StringUtils.removeFormatting( Objects.requireNonNull( InventoryUtils.getItemLore( stack, 11 ) ) ).replace( ",", "" );
          Matcher amoutMatcher = pattern.matcher( amountLore );
          Matcher costMatcher = pattern.matcher( costLore );
          Matcher levelMatcher = pattern.matcher( displayName );
          if ( amoutMatcher.find( ) && costMatcher.find( ) && levelMatcher.find( ) )
          {
            int amount = Integer.parseInt( amoutMatcher.group( ) );
            int cost = Integer.parseInt( costMatcher.group( ) );
            int level = Integer.parseInt( levelMatcher.group( ) );
            
            int amountPerLevel = amount / level;
            float costPer = ( float ) cost / amountPerLevel;
            
            if ( bestRabbitCost == 0 || costPer < bestRabbitCost )
            {
              bestRabbitCost = ( int ) costPer;
              bestRabbitSlot = i;
            }
          }
        }
      }
    }
    
    if ( bestRabbitCost != 0 && chocolatePurse > 0 && bestRabbitCost <= chocolatePurse )
    {
      Random rand = new Random( );
      int min = GumTuneClientConfig.chocolateFactoryMinimumMacroDelay;
      int max = GumTuneClientConfig.chocolateFactoryMaximumMacroDelay;
      Multithreading.schedule(
        ( ) -> {
          ModUtils.sendMessage( "clicked slot " + bestRabbitSlot );
          mc.playerController.windowClick(
            GumTuneClient.mc.thePlayer.openContainer.windowId,
            bestRabbitSlot,
            2,
            3,
            mc.thePlayer
          );
          clicked = true;
        },
        rand.nextInt( ( max - min ) + 1 ) + min,
        TimeUnit.MILLISECONDS
      );
    }
    
    clicked = false;
    bestRabbitCost = 0;
    currentInventory.clear( );
  }
}
