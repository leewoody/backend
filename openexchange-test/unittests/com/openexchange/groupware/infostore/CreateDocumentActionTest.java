
package com.openexchange.groupware.infostore;

import java.sql.SQLException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.database.impl.CreateDocumentAction;
import com.openexchange.tx.UndoableAction;

public class CreateDocumentActionTest extends AbstractInfostoreActionTest {

    @Override
    protected UndoableAction getAction() throws Exception {
        final CreateDocumentAction createAction = new CreateDocumentAction(null);
        createAction.setProvider(getProvider());
        createAction.setContext(getContext());
        createAction.setDocuments(getDocuments());
        createAction.setQueryCatalog(getQueryCatalog());
        return createAction;
    }

    @Override
    protected void verifyPerformed() throws Exception {
        for (final DocumentMetadata doc : getDocuments()) {
            checkInDocTable(doc);
        }
    }

    @Override
    protected void verifyUndone() throws Exception {
        for (final DocumentMetadata doc : getDocuments()) {
            checkNotInDocTable(doc);
        }
    }

    private void checkNotInDocTable(final DocumentMetadata doc) throws OXException, SQLException {
        assertNoResult("SELECT 1 FROM infostore WHERE id = ? and cid = ?", doc.getId(), getContext().getContextId());
    }

    private void checkInDocTable(final DocumentMetadata doc) throws OXException, SQLException {
        assertResult("SELECT 1 FROM infostore WHERE id = ? and cid = ?", doc.getId(), getContext().getContextId());
    }

}
