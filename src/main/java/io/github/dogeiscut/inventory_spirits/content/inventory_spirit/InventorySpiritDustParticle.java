package io.github.dogeiscut.inventory_spirits.content.inventory_spirit;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class InventorySpiritDustParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected InventorySpiritDustParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z);
        this.setColor(0.749f, 1.0f, 0.0f);
        this.friction = 0.96f;
        this.quadSize *= 0.5f;
        this.lifetime = 20 + level.random.nextInt(10);
        this.sprites = sprites;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 15728880;
    }

    @Override
    public void tick() {
        super.tick();

        this.setSpriteFromAge(this.sprites);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            InventorySpiritDustParticle particle = new InventorySpiritDustParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
