import {Subject} from "rxjs";
import {MatPaginatorIntl} from "@angular/material/paginator";
import {Injectable} from "@angular/core";

@Injectable()
export class PaginatorIntlService implements MatPaginatorIntl {
  changes = new Subject<void>();

  // For internationalization, the `$localize` function from
  // the `@angular/localize` package can be used.
  firstPageLabel = `Erste Seite`;
  itemsPerPageLabel = `Ergebnisse pro Seite:`;
  lastPageLabel = `Letzte Seite`;

  // You can set labels to an arbitrary string too, or dynamically compute
  // it through other third-party internationalization libraries.
  nextPageLabel = 'Nächste Seite';
  previousPageLabel = 'Vorherige Seite';

  getRangeLabel(page: number, pageSize: number, length: number): string {
    if (length === 0) {
      return `Seite 1 von 1`;
    }
    const amountPages = Math.ceil(length / pageSize);
    return `Seite ${page + 1} von ${amountPages}`;
  }
}
