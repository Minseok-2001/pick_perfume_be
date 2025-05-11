import { Entity, Property, OneToMany, Collection } from "@mikro-orm/core";
import { BaseEntity } from "./BaseEntity";
import { Perfume } from "./Perfume";

@Entity()
export class Brand extends BaseEntity {
  @Property({ unique: true })
  name!: string;

  @Property({ nullable: true })
  countryCode?: string;

  @Property({ nullable: true })
  countryName?: string;

  @Property({ nullable: true })
  description?: string;

  @Property({ nullable: true })
  foundedYear?: number;

  @Property({ nullable: true })
  website?: string;

  @Property({ nullable: true })
  designer?: string;

  @Property({ default: false })
  isLuxury: boolean = false;

  @Property({ default: false })
  isNiche: boolean = false;

  @Property({ default: false })
  isPopular: boolean = false;

  @Property({ nullable: true })
  url?: string;

  @OneToMany(() => Perfume, (perfume) => perfume.brand)
  perfumes = new Collection<Perfume>(this);

  constructor(
    name: string,
    countryCode?: string,
    countryName?: string,
    description?: string,
    foundedYear?: number,
    website?: string,
    designer?: string,
    isLuxury: boolean = false,
    isNiche: boolean = false,
    isPopular: boolean = false,
    url?: string
  ) {
    super();
    this.name = name;
    this.countryCode = countryCode;
    this.countryName = countryName;
    this.description = description;
    this.foundedYear = foundedYear;
    this.website = website;
    this.designer = designer;
    this.isLuxury = isLuxury;
    this.isNiche = isNiche;
    this.isPopular = isPopular;
    this.url = url;
  }
}
