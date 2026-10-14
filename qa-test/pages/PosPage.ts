import { Page, Locator, expect } from '@playwright/test';
import { pathToFileURL } from 'url';
import path from 'path';

export type ItemId = 'A' | 'B' | 'C' | 'D' | 'E' | 'F';

/** Harga mock: A=30.000, B=45.000, C=20.000, D=60.000, E=49.999, F=50.000 */
export const PRICE: Record<ItemId, number> = { A: 30000, B: 45000, C: 20000, D: 60000, E: 49999, F: 50000 };

export const rp = (n: number) => 'Rp ' + n.toLocaleString('id-ID');

export class PosPage {
  readonly subtotal: Locator;
  readonly discount: Locator;
  readonly total: Locator;
  readonly discountMsg: Locator;
  readonly applyDiscount: Locator;
  readonly cashReceived: Locator;
  readonly change: Locator;
  readonly splitCash: Locator;
  readonly splitQris: Locator;
  readonly qrisPaid: Locator;
  readonly qrisFail: Locator;
  readonly qrisStatus: Locator;
  readonly pay: Locator;
  readonly payMsg: Locator;
  readonly saleStatus: Locator;
  readonly receipt: Locator;
  readonly print: Locator;
  readonly sendDigital: Locator;
  readonly receiptEmail: Locator;
  readonly receiptMsg: Locator;

  constructor(readonly page: Page) {
    const t = (id: string) => page.getByTestId(id);
    this.subtotal = t('subtotal');
    this.discount = t('discount');
    this.total = t('total');
    this.discountMsg = t('discount-msg');
    this.applyDiscount = page.getByRole('button', { name: 'Apply Discount/Promotion' });
    this.cashReceived = page.getByLabel('Cash received');
    this.change = t('change');
    this.splitCash = t('split-cash');
    this.splitQris = t('split-qris');
    this.qrisPaid = page.getByRole('button', { name: 'Simulate QRIS Paid' });
    this.qrisFail = page.getByRole('button', { name: 'Simulate QRIS Failed' });
    this.qrisStatus = t('qris-status');
    this.pay = page.getByRole('button', { name: 'Process Payment' });
    this.payMsg = t('pay-msg');
    this.saleStatus = t('sale-status');
    this.receipt = t('receipt');
    this.print = page.getByRole('button', { name: 'Print Receipt' });
    this.sendDigital = page.getByRole('button', { name: 'Send Digital Receipt' });
    this.receiptEmail = page.getByLabel('Email');
    this.receiptMsg = t('receipt-msg');
  }

  static url(): string {
    return process.env.POS_URL ?? pathToFileURL(path.resolve(__dirname, '../mock/pos.html')).href;
  }

  async goto() {
    await this.page.goto(PosPage.url());
    await expect(this.page.getByRole('heading', { name: 'Sale Transaction' })).toBeVisible();
  }

  async add(item: ItemId, times = 1) {
    for (let i = 0; i < times; i++) await this.page.getByRole('button', { name: `Add Item ${item}` }).click();
  }
  async inc(item: ItemId) { await this.page.getByRole('button', { name: `Increase Item ${item}` }).click(); }
  async dec(item: ItemId) { await this.page.getByRole('button', { name: `Decrease Item ${item}` }).click(); }
  async remove(item: ItemId) { await this.page.getByRole('button', { name: `Remove Item ${item}` }).click(); }

  async chooseMethod(m: 'Cash' | 'QRIS' | 'Split') {
    const radio = m === 'Split'
      ? this.page.getByRole('radio', { name: /Split 40:60/ })
      : this.page.getByRole('radio', { name: m, exact: true });
    await radio.check();
  }
}
