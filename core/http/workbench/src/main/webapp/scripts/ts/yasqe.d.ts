/// <reference path="codemirror.d.ts" />

interface YASQE_Instance extends CodeMirror.EditorFromTextArea {

	getValue(): string;

	setValue(query: string): void;

	refresh(): void;
}

interface YASQE_Config extends CodeMirror.EditorConfiguration {
	consumeShareLink(): any;
}

interface YASQE_Static {
	
	fromTextArea(host: HTMLTextAreaElement, options?: YASQE_Config): YASQE_Instance;
}

declare module "yasqe" {
	export = YASQE;
}

declare var YASQE: YASQE_Static;