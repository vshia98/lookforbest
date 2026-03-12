from .boston_dynamics import BostonDynamicsSpider
from .unitree import UnitreeSpider
from .agility import AgilitySpider
from .dji import DJISpider
from .fanuc import FANUCSpider
from .abb import ABBSpider
from .kuka import KUKASpider
from .irobot import iRobotSpider
from .softbank_robotics import SoftBankRoboticsSpider
from .additional_seeds import AdditionalSeedsSpider

__all__ = [
    "BostonDynamicsSpider",
    "UnitreeSpider",
    "AgilitySpider",
    "DJISpider",
    "FANUCSpider",
    "ABBSpider",
    "KUKASpider",
    "iRobotSpider",
    "SoftBankRoboticsSpider",
    "AdditionalSeedsSpider",
]
