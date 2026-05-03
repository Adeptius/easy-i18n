import { Component, OnInit } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-example',
  template: `
    <!-- Attribute directive (rule: transloco="key") -->

    <!-- Resolved keys → folding, hover documentation, and Ctrl+Click reference work -->
    <h1 transloco="common:primitive.string.multiline"></h1>
    <p transloco="user:object.deeply.nested.structure.description"></p>

    <!-- Unresolved key → inspection error -->
    <span transloco="common:does.not.exist"></span>
  `
})
export class ExampleComponent implements OnInit {

  constructor(private translocoService: TranslocoService) {}

  // --- Call argument (rule: translate(), argument index 0) ---

  // Resolved key → folding, hover documentation, and Ctrl+Click reference work
  greeting: string = this.translocoService.translate('common:primitive.string.sample');
  description: string = this.translocoService.translate('user:object.deeply.nested.structure.description');

  // Unresolved key → inspection error
  broken: string = this.translocoService.translate('common:does.not.exist');

  // --- Call argument (rule: selectTranslate(), argument index 0) ---

  // Resolved key → folding, hover documentation, and Ctrl+Click reference work
  greeting$: Observable<string> = this.translocoService.selectTranslate('billing:object.deeply.nested.structure.description');

  // Unresolved key → inspection error
  broken$: Observable<string> = this.translocoService.selectTranslate('billing:does.not.exist');

  ngOnInit(): void {
    // Resolved key → folding, hover documentation, and Ctrl+Click reference work
    const label = this.translocoService.translate('common:primitive.string.sample');

    // Unresolved key → inspection error
    const missing = this.translocoService.translate('common:does.not.exist');
  }

}
