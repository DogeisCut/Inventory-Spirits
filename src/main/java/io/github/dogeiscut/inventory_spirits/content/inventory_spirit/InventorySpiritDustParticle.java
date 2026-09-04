package io.github.dogeiscut.inventory_spirits.content.inventory_spirit;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class InventorySpiritDustParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected InventorySpiritDustParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.friction = 0.96f;
        this.quadSize *= 0.75f;
        this.lifetime = 20 + level.random.nextInt(10);
        this.sprites = sprites;
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

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            InventorySpiritDustParticle particle = new InventorySpiritDustParticle(level, x, y, z, this.sprites);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
