import { Entity, ManyToOne, Enum, type EnumType } from "@mikro-orm/core";
import { BaseEntity } from "./BaseEntity";
import { Perfume } from "./Perfume";
import { Note } from "./Note";

export enum NoteType {
  TOP = "top",
  MIDDLE = "middle",
  BASE = "base",
}

@Entity()
export class PerfumeNote extends BaseEntity {
  @ManyToOne(() => Perfume)
  perfume!: Perfume;

  @ManyToOne(() => Note)
  note!: Note;

  @Enum({ items: () => NoteType, type: "string" })
  type: NoteType = NoteType.TOP;

  constructor(perfume: Perfume, note: Note, type: NoteType = NoteType.TOP) {
    super();
    this.perfume = perfume;
    this.note = note;
    this.type = type;
  }
}
