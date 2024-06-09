package rosegold.gumtuneclient.modules.macro;

import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import rosegold.gumtuneclient.events.ScreenClosedEvent;
import rosegold.gumtuneclient.utils.GuiUtils;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static rosegold.gumtuneclient.GumTuneClient.mc;

public class ChocolateFactoryMacro
{
  private boolean inChocolateFactory;
  private final ArrayList< ItemStack > currentInventory = new ArrayList<>( );
  
  @SubscribeEvent
  public final void onGuiOpen ( @NotNull GuiOpenEvent event )
  {
    inChocolateFactory = GuiUtils.getInventoryName( event.gui ).equals( "Chocolate Factory" );
    currentInventory.clear( );
  }
  
  @SubscribeEvent
  public void onGuiClose ( ScreenClosedEvent event )
  {
    inChocolateFactory = false;
    currentInventory.clear( );
  }
  
  @SubscribeEvent
  public void onBackgroundDraw ( GuiScreenEvent.BackgroundDrawnEvent event )
  {
    if ( !inChocolateFactory ) return;
    if ( mc.thePlayer.openContainer.inventorySlots.size( ) != currentInventory.size( ) )
    {
      for ( Slot slot : mc.thePlayer.openContainer.inventorySlots )
      {
        currentInventory.add( slot.getStack( ) );
      }
      return;
    }
    
    //ArrayList< ItemStack > skulls = currentInventory.stream( )
     // .filter( itemStack -> itemStack.getItem( ) == Items.skull )
      //.collect( Collectors.toCollection( ArrayList::new ) );
    
    System.out.println( "Current inventory: " + currentInventory.size() );
  }
}
