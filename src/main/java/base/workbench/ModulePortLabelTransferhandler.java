package base.workbench;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.lang.reflect.Constructor;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import modules.Pipe;

public class ModulePortLabelTransferhandler extends TransferHandler {

	private static final long serialVersionUID = -6558974298204317635L;
	private AbstractModulePortLabel draggedPortLabel = null;

	public ModulePortLabelTransferhandler() {
		// TODO Auto-generated constructor stub
	}

	public ModulePortLabelTransferhandler(String property) {
		super(property);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.TransferHandler#getDragImage()
	 */
	@Override
	public Image getDragImage() {
		// TODO Auto-generated method stub
		return super.getDragImage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.TransferHandler#importData(javax.swing.JComponent,
	 * java.awt.datatransfer.Transferable)
	 */
	@Override
	public boolean importData(JComponent comp, Transferable t) {
		if (AbstractModulePortLabel.class.isAssignableFrom(comp.getClass())
				&& t.isDataFlavorSupported(AbstractModulePortLabel.PORTDATAFLAVOR)) {
			AbstractModulePortLabel targetPortLabel = (AbstractModulePortLabel) comp;

			if (AbstractModulePortLabel.class.isAssignableFrom(t.getClass())) {
				AbstractModulePortLabel sourcePortLabel = (AbstractModulePortLabel) t;

				// Determine pipe class supported by both ports
				Class<? extends Pipe> agreeablePipeType = null;
				Iterator<String> targetPortPipeTypes = targetPortLabel
						.getPort().getSupportedPipeClasses().keySet()
						.iterator();
				while (targetPortPipeTypes.hasNext()) {
					String targetPortPipeType = targetPortPipeTypes.next();
					agreeablePipeType = sourcePortLabel.getPort()
							.getSupportedPipeClasses().get(targetPortPipeType);
					if (agreeablePipeType != null)
						break;
				}

				try {
					if (agreeablePipeType != null) {
						Constructor<? extends Pipe> pipeConstructor = agreeablePipeType
								.getConstructor();
						Pipe pipe = pipeConstructor.newInstance();

						targetPortLabel.getPort().addPipe(pipe,
								sourcePortLabel.getPort());
						sourcePortLabel.getPort().addPipe(pipe,
								targetPortLabel.getPort());

						return true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent,
	 * java.awt.datatransfer.DataFlavor[])
	 */
	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		return (AbstractModulePortLabel.class.isAssignableFrom(comp.getClass()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.TransferHandler#createTransferable(javax.swing.JComponent)
	 */
	@Override
	protected Transferable createTransferable(JComponent c) {
		if (AbstractModulePortLabel.class.isAssignableFrom(c.getClass())) {
			AbstractModulePortLabel targetPortLabel = (AbstractModulePortLabel) c;
			return targetPortLabel;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.TransferHandler#exportAsDrag(javax.swing.JComponent,
	 * java.awt.event.InputEvent, int)
	 */
	@Override
	public void exportAsDrag(JComponent comp, InputEvent e, int action) {
		System.out.println("export drag");
		if (action != TransferHandler.LINK)
			return;
		if (AbstractModulePortLabel.class.isAssignableFrom(comp.getClass())) {
			AbstractModulePortLabel draggedPortLabel = (AbstractModulePortLabel) comp;
			this.draggedPortLabel = draggedPortLabel;

			System.out.println("export drag saved");
		}
	}

}
