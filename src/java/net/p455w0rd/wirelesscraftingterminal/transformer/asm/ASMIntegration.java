package net.p455w0rd.wirelesscraftingterminal.transformer.asm;

import java.util.Iterator;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.p455w0rd.wirelesscraftingterminal.helpers.Reflected;
import net.p455w0rd.wirelesscraftingterminal.integration.IntegrationRegistry;
import net.p455w0rd.wirelesscraftingterminal.integration.IntegrationType;
import net.p455w0rd.wirelesscraftingterminal.transformer.Integration;
import cpw.mods.fml.relauncher.FMLRelaunchLog;

@Reflected
public final class ASMIntegration implements IClassTransformer {
	@Reflected
	public ASMIntegration() {

		/**
		 * Side, Display Name, ModID ClassPostFix
		 */

		for (final IntegrationType type : IntegrationType.values()) {
			IntegrationRegistry.INSTANCE.add(type);
		}

	}

	@Nullable
	@Override
	public byte[] transform(final String name, final String transformedName, final byte[] basicClass) {
		if (basicClass == null || transformedName.startsWith("net.p455w0rd.wirelesscraftingterminal.transformer")) {
			return basicClass;
		}

		if (transformedName.startsWith("net.p455w0rd.wirelesscraftingterminal.")) {
			final ClassNode classNode = new ClassNode();
			final ClassReader classReader = new ClassReader(basicClass);
			classReader.accept(classNode, 0);

			try {
				final boolean reWrite = this.removeOptionals(classNode);

				if (reWrite) {
					final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
					classNode.accept(writer);
					return writer.toByteArray();
				}
			} catch (final Throwable t) {
				t.printStackTrace();
			}
		}
		return basicClass;
	}

	@SuppressWarnings("rawtypes")
	private boolean removeOptionals(final ClassNode classNode) {
		boolean changed = false;

		if (classNode.visibleAnnotations != null) {
			for (final AnnotationNode an : classNode.visibleAnnotations) {
				if (this.hasAnnotation(an, Integration.Interface.class)) {
					if (this.stripInterface(classNode, Integration.Interface.class, an)) {
						changed = true;
					}
				} else if (this.hasAnnotation(an, Integration.InterfaceList.class)) {
					for (final Object o : ((Iterable) an.values.get(1))) {
						if (this.stripInterface(classNode, Integration.InterfaceList.class, (AnnotationNode) o)) {
							changed = true;
						}
					}
				}
			}
		}

		final Iterator<MethodNode> i = classNode.methods.iterator();
		while (i.hasNext()) {
			final MethodNode mn = i.next();

			if (mn.visibleAnnotations != null) {
				for (final AnnotationNode an : mn.visibleAnnotations) {
					if (this.hasAnnotation(an, Integration.Method.class)) {
						if (this.stripMethod(classNode, mn, i, Integration.Method.class, an)) {
							changed = true;
						}
					}
				}
			}
		}

		if (changed) {
			this.log("Updated " + classNode.name);
		}

		return changed;
	}

	private boolean hasAnnotation(final AnnotationNode ann, final Class<?> annotation) {
		return ann.desc.equals(Type.getDescriptor(annotation));
	}

	private boolean stripInterface(final ClassNode classNode, final Class<?> class1, final AnnotationNode an) {
		if (an.values.size() != 4) {
			throw new IllegalArgumentException("Unable to handle Interface annotation on " + classNode.name);
		}

		String iFace = null;

		if (an.values.get(0).equals("iface")) {
			iFace = (String) an.values.get(1);
		} else if (an.values.get(2).equals("iface")) {
			iFace = (String) an.values.get(3);
		}

		String iName = null;
		if (an.values.get(0).equals("iname")) {
			iName = ((String[]) an.values.get(1))[1];
		} else if (an.values.get(2).equals("iname")) {
			iName = ((String[]) an.values.get(3))[1];
		}

		if (iName != null && iFace != null) {
			final IntegrationType type = IntegrationType.valueOf(iName);
			if (!IntegrationRegistry.INSTANCE.isEnabled(type)) {
				this.log("Removing Interface " + iFace + " from " + classNode.name + " because " + iName
						+ " integration is disabled.");
				classNode.interfaces.remove(iFace.replace('.', '/'));
				return true;
			} else {
				this.log("Allowing Interface " + iFace + " from " + classNode.name + " because " + iName
						+ " integration is enabled.");
			}
		} else {
			throw new IllegalStateException("Unable to handle Method annotation on " + classNode.name);
		}

		return false;
	}

	@SuppressWarnings("rawtypes")
	private boolean stripMethod(final ClassNode classNode, final MethodNode mn, final Iterator<MethodNode> i,
			final Class class1, final AnnotationNode an) {
		if (an.values.size() != 2) {
			throw new IllegalArgumentException("Unable to handle Method annotation on " + classNode.name);
		}

		String iName = null;

		if (an.values.get(0).equals("iname")) {
			iName = ((String[]) an.values.get(1))[1];
		}

		if (iName != null) {
			final IntegrationType type = IntegrationType.valueOf(iName);
			if (!IntegrationRegistry.INSTANCE.isEnabled(type)) {
				this.log("Removing Method " + mn.name + " from " + classNode.name + " because " + iName
						+ " integration is disabled.");
				i.remove();
				return true;
			} else {
				this.log("Allowing Method " + mn.name + " from " + classNode.name + " because " + iName
						+ " integration is enabled.");
			}
		} else {
			throw new IllegalStateException("Unable to handle Method annotation on " + classNode.name);
		}

		return false;
	}

	private void log(final String string) {
		FMLRelaunchLog.log("WCT-CORE", Level.INFO, string);
	}
}
