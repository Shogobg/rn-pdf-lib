import { PDFPage, PageAction } from "./PDFPage";

type DocumentAction = {
  path: string;
  pages: PageAction[];
  loadPages?: PageAction[],
  modifyPages?: PageAction[];
};
/**
* Here is some docs...
*/
export class PDFDocument {
  /**
   * Create a new PDFDocument that will be written at the specified path
   * @param {string} path - The absolute file path for this document to be
   *                        written to.
   */
  static create: (path: string) => PDFDocument;
  static modify: (path: string) => PDFDocument;
  document: DocumentAction;
  setPath: (path: string) => PDFDocument;
  modifyPage: ({ page }: PDFPage) => PDFDocument;
  modifyPages: (...pages: PDFPage[]) => PDFDocument;
  loadPage: ({ page }: PDFPage) => PDFDocument;
  loadPages: (...pages: PDFPage[]) => PDFDocument;
  addPage: ({ page }: PDFPage) => PDFDocument;
  addPages: (...pages: PDFPage[]) => PDFDocument;
  write: () => any;
}
export default {};
