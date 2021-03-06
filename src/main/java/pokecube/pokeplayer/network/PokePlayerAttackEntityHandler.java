package pokecube.pokeplayer.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.events.CommandAttackEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.IHasCommands.IMobCommandHandler;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

// Wrapper to ensure player attacks entity as pokeplayer
public class PokePlayerAttackEntityHandler implements IMobCommandHandler
{
    public int targetId;

    public PokePlayerAttackEntityHandler()
    {
    }

    public PokePlayerAttackEntityHandler(Integer targetId)
    {
        this.targetId = targetId;
    }

    @Override
    public void handleCommand(IPokemob pokemob)
    {
        World world = pokemob.getEntity().getEntityWorld();
        Entity target = PokecubeMod.core.getEntityProvider().getEntity(world, targetId, true);
        if (target == null || !(target instanceof EntityLivingBase))
        {
            if (target == null) throw new IllegalArgumentException("Target Mob cannot be null!");
            else throw new IllegalArgumentException("Invalid target: " + target);
        }
        String moveName = "";
        int currentMove = pokemob.getMoveIndex();
        MinecraftForge.EVENT_BUS.post(new CommandAttackEvent(pokemob.getEntity(), target));
        if (currentMove != 5 && MovesUtils.canUseMove(pokemob))
        {
            Move_Base move = MovesUtils.getMoveFromName(pokemob.getMoves()[currentMove]);
            moveName = MovesUtils.getUnlocalizedMove(move.getName());
            if (move.isSelfMove())
            {
                pokemob.executeMove(pokemob.getEntity(), null, 0);
            }
            else
            {
                ITextComponent mess = new TextComponentTranslation("pokemob.command.attack",
                        pokemob.getPokemonDisplayName(), target.getDisplayName(),
                        new TextComponentTranslation(moveName));
                pokemob.displayMessageToOwner(mess);
                pokemob.getEntity().setAttackTarget((EntityLivingBase) target);
                if (target instanceof EntityLiving) ((EntityLiving) target).setAttackTarget(pokemob.getEntity());
                // Checks if within range
                float dist = target.getDistance(pokemob.getEntity());
                if (dist < PokecubeMod.core.getConfig().combatDistance)
                {
                    pokemob.executeMove(target, Vector3.getNewVector().set(target), dist);
                }
            }
        }
    }

    @Override
    public void writeToBuf(ByteBuf buf)
    {
        buf.writeInt(targetId);
    }

    @Override
    public void readFromBuf(ByteBuf buf)
    {
        targetId = buf.readInt();
    }

    @Override
    public IMobCommandHandler setFromOwner(boolean owner)
    {
        return this;
    }

    @Override
    public boolean fromOwner()
    {
        return false;
    }

}
