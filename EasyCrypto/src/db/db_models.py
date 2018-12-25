from sqlalchemy import Column, Integer, String, Numeric, ForeignKey
from sqlalchemy.types import DateTime
from sqlalchemy.orm import relationship

from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()


def load_args(object_to_load, args):
    """ Loads model object with given data

    :param object_to_load: object to load with arguments
    :param args: data to load
    """
    object_to_load.__dict__.update(args)

class Website(Base):
    __tablename__ = 'website'

    website_id = Column(Integer, primary_key=True)
    name = Column(String, nullable=False)
    address = Column(String, nullable=False)
    protocol = Column(String, nullable=False)

    action = relationship("Action", back_populates="website", cascade="all, delete, delete-orphan")
    ticker = relationship("Ticker", back_populates="website", cascade="all, delete, delete-orphan")

class Action(Base):
    __tablename__ = 'action'

    action_id = Column(Integer, primary_key=True)
    website_id = Column(Integer, ForeignKey("website.website_id"), nullable=False)
    address = Column(String, nullable=False)

    website = relationship("Website", back_populates="action")
    specification = relationship("Specification", back_populates="action", cascade="all, delete, delete-orphan")


class Parameter(Base):
    __tablename__ = 'parameter'

    parameter_id = Column(Integer, primary_key=True)
    name = Column(String, nullable=False)
    type = Column(String, nullable=False)

    specification = relationship("Specification", back_populates="parameter", cascade="all, delete, delete-orphan")


class Specification(Base):
    __tablename__ = 'specification'

    specification_id = Column(Integer, primary_key=True)
    action_id = Column(Integer, ForeignKey("action.action_id"), nullable=False)
    parameter_id = Column(Integer, ForeignKey("parameter.parameter_id"), nullable=False)

    action = relationship("Action", back_populates="specification")
    parameter = relationship("Parameter", back_populates="specification")


class Ticker(Base):
    __tablename__ = 'ticker'

    ticker_id = Column(Integer, primary_key=True)
    website_id = Column(Integer, ForeignKey("website.website_id"), nullable=False)

    currency_pair = Column(String, nullable=False)
    last_price = Column(Numeric(precision=38, scale=17), nullable=False)
    open_price = Column(Numeric(precision=38, scale=17), nullable=False)
    highest_bid = Column(Numeric(precision=38, scale=17), nullable=False)
    lowest_ask = Column(Numeric(precision=38, scale=17), nullable=False)
    timestamp = Column(DateTime(), nullable=False)

    website = relationship("Website", back_populates="ticker")