package com.example.admin_ingresos.ui.icons

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.*
import com.example.admin_ingresos.data.Category

/**
 * Sistema de mapeo de iconos usando Lucide Icons
 * Proporciona iconos consistentes y profesionales para toda la aplicación
 */
object LucideIconMapper {

    fun getIconFromEmoji(emoji: String): ImageVector {
        return when (emoji) {
            // Alimentación y restaurantes
            "🍕", "🍔", "🥘", "🍽️", "🥗", "🍎", "🥙", "🌮", "🥪" -> Lucide.UtensilsCrossed
            "☕", "🥤", "🍺", "🍷", "🧃" -> Lucide.Coffee
            "🛒", "🏪", "🛍️" -> Lucide.ShoppingCart

            // Transporte
            "🚗", "🚙", "🚘" -> Lucide.Car
            "⛽", "🛣️" -> Lucide.Fuel
            "🚌", "🚊", "🚇" -> Lucide.Bus
            "🚕", "🚖" -> Lucide.Car // Taxi
            "🚲", "🛴" -> Lucide.Bike
            "✈️", "🛫", "🛬" -> Lucide.Plane
            "🚢", "⛵" -> Lucide.Ship
            "🚂", "🚆", "🚄" -> Lucide.TrainFront

            // Entretenimiento
            "🎬", "🎭", "🎪" -> Lucide.Film
            "🎮", "🕹️" -> Lucide.Gamepad2
            "🎵", "🎶", "🎤" -> Lucide.Music
            "📺", "📻" -> Lucide.Tv
            "🎨", "🖼️" -> Lucide.Palette
            "📚", "📖", "📑" -> Lucide.Book
            "🏃", "⚽", "🏀", "🎾" -> Lucide.Dumbbell

            // Servicios y utilidades
            "💡", "🔌" -> Lucide.Zap
            "💧", "🚿" -> Lucide.Droplets
            "📶", "📱", "📞" -> Lucide.Smartphone
            "🌐", "💻", "🖥️" -> Lucide.Wifi
            "🔥", "❄️" -> Lucide.Thermometer
            "🗑️", "♻️" -> Lucide.Trash2

            // Compras y ropa
            "👕", "👔", "👗", "👠", "🧥", "👖", "👒" -> Lucide.Shirt
            "💍", "⌚", "👜" -> Lucide.Gem

            // Salud y cuidado personal
            "🏥", "🏩" -> Lucide.Hospital
            "💊", "🩺" -> Lucide.Pill
            "⚕️", "🔬" -> Lucide.Stethoscope
            "💉", "🩹" -> Lucide.Syringe
            "🦷", "🪥" -> Lucide.Smile
            "💄", "🧴", "🧼" -> Lucide.Sparkles

            // Educación y trabajo
            "📝" -> Lucide.BookOpen
            "🎓", "🏫", "📐" -> Lucide.GraduationCap
            "✏️", "🖊️", "📏" -> Lucide.PenTool
            "💼", "🏢" -> Lucide.Briefcase
            "⌨️" -> Lucide.Laptop

            // Hogar y mantenimiento
            "🏠", "🏡", "🏘️" -> Lucide.House
            "🔧", "🛠️", "🔨" -> Lucide.Wrench
            "🪑", "🛏️", "🚪" -> Lucide.Armchair
            "🧹", "🧽" -> Lucide.Sparkles
            "🌱", "🌿", "🪴" -> Lucide.Leaf

            // Finanzas y dinero
            "💰", "💵", "💴", "💶", "💷" -> Lucide.DollarSign
            "💳", "💎" -> Lucide.CreditCard
            "🏦", "🏧" -> Lucide.Landmark
            "📊", "📈", "📉" -> Lucide.TrendingUp
            "💸", "🪙" -> Lucide.Coins

            // Regalos y eventos
            "🎁", "🎉", "🎈" -> Lucide.Gift
            "🎂", "🍰", "🧁" -> Lucide.Cake
            "💝", "💌" -> Lucide.Heart
            "🎊", "🏆", "🥇" -> Lucide.Award

            // Tecnología
            "📷", "📹" -> Lucide.Camera
            "🎧", "🔊" -> Lucide.Headphones

            // Viajes y turismo
            "🧳", "🎒" -> Lucide.Luggage
            "🗺️", "🧭" -> Lucide.Map
            "🏨" -> Lucide.Building
            "🏖️", "🏝️" -> Lucide.Trees
            "⛰️", "🏔️" -> Lucide.Mountain
            "🎡", "🎢" -> Lucide.PartyPopper

            // Mascotas y animales
            "🐕", "🐶" -> Lucide.Dog
            "🐱", "🐈" -> Lucide.Cat
            "🐟", "🐠" -> Lucide.Fish
            "🐦", "🦜" -> Lucide.Bird

            // Seguros y emergencias
            "🚨", "🚑" -> Lucide.Siren
            "🔒", "🛡️" -> Lucide.Shield
            "⚠️", "🚧" -> Lucide.TriangleAlert

            // Default fallback
            else -> Lucide.Tag
        }
    }

    fun getIconFromCategoryName(categoryName: String): ImageVector {
        return when (categoryName.lowercase()) {
            "alimentación", "comida", "restaurante", "supermercado", "mercado" -> Lucide.UtensilsCrossed
            "bebidas", "café", "bar" -> Lucide.Coffee
            "transporte", "gasolina", "combustible", "taxi", "uber", "cabify" -> Lucide.Car
            "bus", "metro", "transporte público" -> Lucide.Bus
            "avión", "vuelo", "aeropuerto" -> Lucide.Plane
            "bicicleta", "patinete" -> Lucide.Bike
            "entretenimiento", "diversión", "ocio", "cine", "teatro", "espectáculos" -> Lucide.Film
            "videojuegos", "juegos" -> Lucide.Gamepad2
            "música", "conciertos" -> Lucide.Music
            "deportes", "gimnasio", "fitness" -> Lucide.Dumbbell
            "servicios", "luz", "electricidad" -> Lucide.Zap
            "agua", "acueducto" -> Lucide.Droplets
            "internet", "telefonía", "móvil" -> Lucide.Wifi
            "gas", "calefacción" -> Lucide.Thermometer
            "compras", "ropa", "vestimenta", "shopping", "centro comercial" -> Lucide.ShoppingCart
            "joyería", "accesorios" -> Lucide.Gem
            "salud", "médico", "hospital" -> Lucide.Hospital
            "farmacia", "medicamentos" -> Lucide.Pill
            "dentista", "odontología" -> Lucide.Smile
            "belleza", "cosmética" -> Lucide.Sparkles
            "educación", "estudio", "universidad" -> Lucide.GraduationCap
            "cursos", "capacitación", "libros", "material de estudio" -> Lucide.BookOpen
            "trabajo", "oficina", "negocio" -> Lucide.Briefcase
            "tecnología", "software" -> Lucide.Laptop
            "hogar", "casa", "vivienda" -> Lucide.House
            "mantenimiento", "reparaciones" -> Lucide.Wrench
            "muebles", "decoración" -> Lucide.Armchair
            "jardinería", "plantas" -> Lucide.Leaf
            "limpieza" -> Lucide.Sparkles
            "finanzas", "banco", "inversión" -> Lucide.Landmark
            "ahorros", "dinero" -> Lucide.PiggyBank
            "tarjetas", "crédito" -> Lucide.CreditCard
            "regalos", "obsequios" -> Lucide.Gift
            "celebraciones", "fiestas" -> Lucide.PartyPopper
            "cumpleaños" -> Lucide.Cake
            "viajes", "turismo", "vacaciones" -> Lucide.Luggage
            "hotel", "hospedaje" -> Lucide.Building
            "mapas", "navegación" -> Lucide.Map
            "seguros", "protección" -> Lucide.Shield
            "emergencias" -> Lucide.Siren
            "mascotas", "perros" -> Lucide.Dog
            "gatos" -> Lucide.Cat
            "veterinario" -> Lucide.HeartPulse
            "inversiones", "acciones" -> Lucide.TrendingUp
            "criptomonedas", "crypto" -> Lucide.Coins
            else -> Lucide.Tag
        }
    }

    fun getCategoryIcon(category: Category): ImageVector {
        if (category.icon.isNotEmpty()) {
            // 1) Try if the stored value is an emoji mapped to an icon
            val iconFromEmoji = getIconFromEmoji(category.icon)
            if (iconFromEmoji != Lucide.Tag) {
                return iconFromEmoji
            }

            // 2) Try if the stored value is a savings-goal key (e.g. "car", "emergency")
            val savingsIcon = getSavingsGoalIcon(category.icon)
            if (savingsIcon != Lucide.Tag) {
                return savingsIcon
            }

            // 3) Try to match against the available category icon names (case-insensitive)
            val available = getAvailableCategoryIcons().find { it.name.equals(category.icon, ignoreCase = true) }
            if (available != null) {
                val vector = getIconFromEmoji(available.icon)
                if (vector != Lucide.Tag) return vector
            }

            // 4) Finally, try to interpret the stored value as a category name
            val iconFromStoredName = getIconFromCategoryName(category.icon)
            if (iconFromStoredName != Lucide.Tag) {
                return iconFromStoredName
            }
        }

        // Fallback: infer icon from category name
        return getIconFromCategoryName(category.name)
    }

    fun getTransactionTypeIcon(type: String): ImageVector {
        return when (type.lowercase()) {
            "ingreso", "income" -> Lucide.TrendingUp
            "gasto", "expense" -> Lucide.TrendingDown
            else -> Lucide.DollarSign
        }
    }

    fun getPaymentMethodIcon(paymentMethod: String): ImageVector {
        return when (paymentMethod.lowercase()) {
            "efectivo", "cash" -> Lucide.Banknote
            "tarjeta de crédito", "crédito", "credit card" -> Lucide.CreditCard
            "tarjeta de débito", "débito", "debit card" -> Lucide.CreditCard
            "transferencia", "transfer", "banco" -> Lucide.Landmark
            "nequi", "daviplata", "digital", "wallet" -> Lucide.Smartphone
            "paypal", "online" -> Lucide.Globe
            "cheque" -> Lucide.FileText
            else -> Lucide.Wallet
        }
    }

    fun getNavigationIcon(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            "home", "casa" -> Lucide.House
            "transactions", "transacciones", "receipt" -> Lucide.Receipt
            "reports", "reportes" -> Lucide.ChartArea
            "settings", "configuracion" -> Lucide.Settings
            "profile", "perfil" -> Lucide.User
            "notifications", "notificaciones" -> Lucide.Bell
            "search", "buscar" -> Lucide.Search
            "filter", "filtrar" -> Lucide.Filter
            "add", "agregar" -> Lucide.Plus
            "edit", "editar" -> Lucide.PenTool
            "delete", "eliminar" -> Lucide.Trash2
            "back", "atras" -> Lucide.ArrowLeft
            "forward", "adelante" -> Lucide.ArrowRight
            "arrowup" -> Lucide.ArrowUp
            "arrowdown" -> Lucide.ArrowDown
            "up" -> Lucide.ChevronUp
            "down" -> Lucide.ChevronDown
            "close", "cerrar" -> Lucide.X
            "check", "confirmar" -> Lucide.Check
            "calendar", "calendario" -> Lucide.Calendar
            "calendardays" -> Lucide.CalendarDays
            "calendardown" -> Lucide.CalendarClock
            "calendarup" -> Lucide.CalendarPlus
            "clock", "reloj" -> Lucide.Clock
            "info", "informacion" -> Lucide.Info
            "warning", "advertencia" -> Lucide.TriangleAlert
            "error" -> Lucide.CircleAlert
            "success", "exitoso" -> Lucide.CircleCheck
            "camera", "camara" -> Lucide.Camera
            "upload", "subir" -> Lucide.Upload
            "download", "descargar" -> Lucide.Download
            "share", "compartir" -> Lucide.Share
            "menu" -> Lucide.Menu
            "more", "mas" -> Lucide.Ellipsis
            "dollarsign", "dollar" -> Lucide.DollarSign
            "money" -> Lucide.Coins
            "list" -> Lucide.List
            "history" -> Lucide.History
            "rewind" -> Lucide.Rewind
            "infinity" -> Lucide.Infinity
            "archive" -> Lucide.Archive
            else -> Lucide.Tag
        }
    }
    
    // --- FUNCIÓN DEDICADA PARA METAS DE AHORRO ---
    fun getSavingsGoalIcon(goalKey: String): ImageVector {
        return when (goalKey.lowercase()) {
            "emergency" -> Lucide.Shield
            "car" -> Lucide.Car
            "house" -> Lucide.House
            "travel" -> Lucide.Plane
            "education" -> Lucide.GraduationCap
            "retirement" -> Lucide.Landmark
            "tech" -> Lucide.Laptop
            "wedding" -> Lucide.Heart
            "business" -> Lucide.Briefcase
            "investment" -> Lucide.TrendingUp
            "repairs" -> Lucide.Wrench
            "gift" -> Lucide.Gift
            "lock" -> Lucide.Lock
            "other" -> Lucide.PiggyBank
            else -> Lucide.Tag
        }
    }
    
    fun getAvailableCategoryIcons(): List<CategoryIconOption> {
        return listOf(
            CategoryIconOption("UtensilsCrossed", "Comida", "🍽️"),
            CategoryIconOption("Car", "Transporte", "🚗"),
            CategoryIconOption("House", "Hogar", "🏠"),
            CategoryIconOption("Briefcase", "Trabajo", "💼"),
            CategoryIconOption("GraduationCap", "Educación", "🎓"),
            CategoryIconOption("HeartPulse", "Salud", "⚕️"),
            CategoryIconOption("Film", "Entretenimiento", "🎬"),
            CategoryIconOption("Shirt", "Ropa", "👕"),
            CategoryIconOption("Landmark", "Finanzas", "💰"),
            CategoryIconOption("Gift", "Regalos", "🎁"),
            CategoryIconOption("Luggage", "Viajes", "🧳"),
            CategoryIconOption("Zap", "Servicios", "⚡"),
            CategoryIconOption("ShoppingCart", "Compras", "🛒"),
            CategoryIconOption("Dog", "Mascotas", "🐕"),
            CategoryIconOption("Dumbbell", "Deportes", "🏋️"),
            CategoryIconOption("Music", "Música", "🎵"),
            CategoryIconOption("Book", "Libros", "📚"),
            CategoryIconOption("Coffee", "Café", "☕"),
            CategoryIconOption("Gamepad2", "Juegos", "🎮"),
            CategoryIconOption("Shield", "Seguros", "🛡️")
        )
    }

    object Navigation {
        val home = getNavigationIcon("home")
        val transactions = getNavigationIcon("transactions")
        val reports = getNavigationIcon("reports")
        val settings = getNavigationIcon("settings")
        val profile = getNavigationIcon("profile")
        val notifications = getNavigationIcon("notifications")
        val search = getNavigationIcon("search")
        val filter = getNavigationIcon("filter")
        val add = getNavigationIcon("add")
        val edit = getNavigationIcon("edit")
        val delete = getNavigationIcon("delete")
        val back = getNavigationIcon("back")
        val forward = getNavigationIcon("forward")
        val up = getNavigationIcon("up")
        val down = getNavigationIcon("down")
        val close = getNavigationIcon("close")
        val check = getNavigationIcon("check")
        val calendar = getNavigationIcon("calendar")
        val clock = getNavigationIcon("clock")
        val info = getNavigationIcon("info")
        val warning = getNavigationIcon("warning")
        val error = getNavigationIcon("error")
        val success = getNavigationIcon("success")
        val camera = getNavigationIcon("camera")
        val upload = getNavigationIcon("upload")
        val download = getNavigationIcon("download")
        val share = getNavigationIcon("share")
        val menu = getNavigationIcon("menu")
        val more = getNavigationIcon("more")
        val dollarSign = getNavigationIcon("dollarSign")
    }
}

data class CategoryIconOption(
    val name: String,
    val description: String,
    val icon: String // Emoji
)