package de.siphalor.amecs.impl.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

@Environment(EnvType.CLIENT)
public class AmecsAPIMixinConfig implements IMixinConfigPlugin {
	private final String MOUSE_CLASS_INTERMEDIARY = "net.minecraft.class_312";
	private final String SCREEN_CLASS_INTERMEDIARY = "net.minecraft.class_437";
	private final MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
	private String mouseClassRemapped;
	private String screenClassRemapped;
	private String screenMouseScrolledRemapped;

	@Override
	public void onLoad(String mixinPackage) {
		mouseClassRemapped = mappingResolver.mapClassName("intermediary", MOUSE_CLASS_INTERMEDIARY);
		screenClassRemapped = mappingResolver.mapClassName("intermediary", SCREEN_CLASS_INTERMEDIARY).replace('.', '/');
		screenMouseScrolledRemapped = mappingResolver.mapMethodName("intermediary", SCREEN_CLASS_INTERMEDIARY, "mouseScrolled", "(DDD)Z");
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.endsWith("MixinNMUKKeyBindingHelper")) {
			return FabricLoader.getInstance().isModLoaded("nmuk");
		}
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		if (targetClassName.equals(mouseClassRemapped)) {
			String onMouseScrollRemapped = mappingResolver.mapMethodName("intermediary", MOUSE_CLASS_INTERMEDIARY, "method_1598", "(JDD)V");

			for (MethodNode method : targetClass.methods) {
				if (onMouseScrollRemapped.equals(method.name)) {
					targetClass.methods.remove(method);
					method.accept(new OnMouseScrollTransformer(
							targetClass.visitMethod(method.access, method.name, method.desc, method.signature, method.exceptions.toArray(new String[0])),
							method.access, method.name, method.desc
					));
					break;
				}
			}
		}
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	// The purpose of this is to capture the return value of the currentScreen.mouseScrolled call.
	// Other mods also require this so this would lead to @Redirect conflicts when done via mixins.
	private class OnMouseScrollTransformer extends GeneratorAdapter {
		private Label varStart;
		private Label varEnd;

		protected OnMouseScrollTransformer(MethodVisitor methodVisitor, int access, String name, String descriptor) {
			super(Opcodes.ASM9, methodVisitor, access, name, descriptor);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
			if (opcode == Opcodes.INVOKEVIRTUAL && screenClassRemapped.equals(owner) && screenMouseScrolledRemapped.equals(name)) {
				varStart = new Label();
				super.visitLabel(varStart);
				super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
				// 23 is arbitrarily chosen. It seems to get reduced to a lower fitting number later.
				super.visitVarInsn(Opcodes.ISTORE, 23);
				// Another Opcode is necessary for Mixin to recognize the local variable.
				// This could technically be a noop too, but since there's a POP after the INVOKE anyways,
				// it's simpler to just push the var onto the stack
				super.visitVarInsn(Opcodes.ILOAD, 23);
				varEnd = new Label();
				super.visitLabel(varEnd);
				return;
			}
			super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
		}

		@Override
		public void visitEnd() {
			super.visitLocalVariable("handled", "Z", null, varStart, varEnd, 23);
			super.visitEnd();
		}
	}
}
